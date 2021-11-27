package com.example.quizapp.utils

import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.relations.FacultyCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.entities.sync.LocallyFilledQuestionnaireToUpload
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.*
import com.example.quizapp.model.ktor.responses.DeleteFilledQuestionnaireResponse.*
import com.example.quizapp.model.ktor.responses.DeleteFilledQuestionnaireResponse.DeleteFilledQuestionnaireResponseType.*
import com.example.quizapp.model.ktor.responses.DeleteQuestionnaireResponse.*
import com.example.quizapp.model.ktor.responses.DeleteUserResponse.*
import com.example.quizapp.model.ktor.responses.InsertFilledQuestionnairesResponse.*
import com.example.quizapp.model.ktor.responses.InsertQuestionnairesResponse.*
import com.example.quizapp.model.ktor.status.SyncStatus
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

//TODO -> Filled Questionnaires sollen vom backend runtergeladen werden, wenn lokal kein [LocallyFilledQuestionnaireToUpload] vorhanden ist
// Wenn es vorhanden ist, werden stattdessen die Antworten hochgeladen

@Singleton
class BackendSyncer @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository
) {

    suspend fun syncData() = withContext(IO) {
        val facultyAndCursesOfStudiesAsync = async {
            syncFacultiesAndCoursesOfStudies(getFacultiesAsync(), getCourseOfStudiesAsync())
        }

        val questionnaireAsync = async {
            synAllQuestionnaireData()
        }

        facultyAndCursesOfStudiesAsync.await()
        questionnaireAsync.await()
    }

    private suspend fun getFacultiesAsync() = withContext(IO) {
        async {
            try {
                backendRepository.getFacultySynchronizationData(localRepository.getFacultyIdsWithTimestamp())
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun getCourseOfStudiesAsync() = withContext(IO) {
        async {
            try {
                backendRepository.getCourseOfStudiesSynchronizationData(localRepository.getCourseOfStudiesIdsWithTimestamp())
            } catch (e: Exception) {
                null
            }
        }
    }

    //TODO -> Subjects sind nur Strings, also irrelevant hier
    private suspend fun syncSubject() = withContext(IO) {

    }


    //TODO -> Update funktioniert noch nicht richtig, anschauen!
    suspend fun syncFacultiesAndCoursesOfStudies(
        faultyResponse: Deferred<SyncFacultiesResponse?>,
        cosResponse: Deferred<SyncCoursesOfStudiesResponse?>
    ) {

        faultyResponse.await()?.let { facultyResponse ->
            localRepository.insert(facultyResponse.facultiesToInsert.map(DataMapper::mapMongoFacultyToRoomFaculty))
            localRepository.update(facultyResponse.facultiesToUpdate.map(DataMapper::mapMongoFacultyToRoomFaculty))
            localRepository.deleteFacultiesWith(facultyResponse.facultyIdsToDelete)

            cosResponse.await()?.let { cosResponse ->
                val allLocalFaculties = localRepository.getFacultyIdsWithTimestamp().map(FacultyIdWithTimeStamp::facultyId)
                val facultyCosRelations: MutableList<FacultyCourseOfStudiesRelation>

                (cosResponse.coursesOfStudiesToInsert.map(DataMapper::mapMongoCourseOfStudiesToRoomCourseOfStudies)).let {
                    localRepository.insert(it.map(Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>>::first))
                    facultyCosRelations = it.flatMap(Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>>::second).toMutableList()
                }

                (cosResponse.coursesOfStudiesToUpdate.map(DataMapper::mapMongoCourseOfStudiesToRoomCourseOfStudies)).let {
                    localRepository.update(it.map(Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>>::first))
                    facultyCosRelations += it.flatMap(Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>>::second)
                }

                localRepository.deleteCoursesOfStudiesWith(cosResponse.courseOfStudiesIdsToDelete)

                facultyCosRelations.filter { it.facultyId in allLocalFaculties }.let { filteredFacultyCosRelations ->
                    localRepository.insert(filteredFacultyCosRelations)
                }
            }
        }
    }




    //TODO -> Die müssen auch ignoriert werden beim runterladen von den Antworten
    //TODO -> IGNORE bei finden von den MongoFilledQuestionnaires
    //val locallyDeletedFilledQuestionnaireIds = localRepository.getLocallyDeletedFilledQuestionnaireIds()
    suspend fun synAllQuestionnaireData() = withContext(IO) {
        localRepository.unsyncAllSyncingQuestionnaires()

        val unsyncedQuestionnaireIdsAsync = async {
            localRepository.findAllNonSyncedQuestionnaireIds()
        }

        val locallyDeletedQuestionnaireIdsAsync = async {
            localRepository.getLocallyDeletedQuestionnaireIds()
        }

        val syncQuestionnairesAsync = async {
            syncQuestionnaires(
                { locallyDeletedQuestionnaireIdsAsync.await() },
                { unsyncedQuestionnaireIdsAsync.await() }
            )
        }

        val locallyDeletedAsync = async {
            syncLocallyDeletedQuestionnaires {
                locallyDeletedQuestionnaireIdsAsync.await()
            }
        }

        val locallyAnsweredAsync = async {
            syncLocallyAnsweredQuestionnaires {
                unsyncedQuestionnaireIdsAsync.await()
            }
        }

        syncQuestionnairesAsync.await()
        locallyDeletedAsync.await()
        locallyAnsweredAsync.await()
    }

    private suspend fun syncQuestionnaires(
        locallyDeletedQuestionnairesProvider: suspend (() -> List<LocallyDeletedQuestionnaire>) = { localRepository.getLocallyDeletedQuestionnaireIds() },
        unsyncedQuestionnaireIdsProvider: suspend (() -> List<String>) = { localRepository.findAllNonSyncedQuestionnaireIds() }
    ) = withContext(IO) {
        val syncedQuestionnaires = localRepository.findAllSyncedQuestionnaires()

        runCatching {
            backendRepository.getQuestionnairesForSyncronization(
                syncedQuestionnaires.map(CompleteQuestionnaire::asQuestionnaireIdWithTimestamp),
                unsyncedQuestionnaireIdsProvider(),
                locallyDeletedQuestionnairesProvider()
            )
        }.onSuccess { response ->
            val insertAsync = async {
                mapToCompleteQuestionnaire(syncedQuestionnaires, response).let { questionnairesToInsert ->
                    localRepository.insertCompleteQuestionnaires(questionnairesToInsert)
                }

                response.mongoQuestionnaires.flatMap(DataMapper::mapMongoQuestionnaireToRoomQuestionnaireCourseOfStudiesRelation).let {
                    localRepository.insert(it)
                }
            }

            val updateAsync = async {
                unsyncNonExistentQuestionnaires(syncedQuestionnaires, response).let { questionnairesToUnsync ->
                    localRepository.update(questionnairesToUnsync)
                }
            }

            insertAsync.await()
            updateAsync.await()

            uploadUnsyncedQuestionnaires()
        }
    }

    private fun mapToCompleteQuestionnaire(
        syncedCompleteQuestionnaires: List<CompleteQuestionnaire>,
        response: SyncQuestionnairesResponse
    ) = response.mongoQuestionnaires.map(DataMapper::mapMongoQuestionnaireToRoomCompleteQuestionnaire).onEach { q ->
        response.mongoFilledQuestionnaires.firstOrNull { it.questionnaireId == q.questionnaire.id }?.let { remoteFilledQuestionnaire ->
            q.questionsWithAnswers = q.questionsWithAnswers.map { qwa ->
                selectAnswers(qwa, remoteFilledQuestionnaire.allSelectedAnswerIds)
            }.toMutableList()
        } ?: syncedCompleteQuestionnaires.firstOrNull { it.questionnaire.id == q.questionnaire.id }?.let { locallySyncedQuestionnaire ->
            q.questionsWithAnswers = q.questionsWithAnswers.map { qwa ->
                selectAnswers(qwa, locallySyncedQuestionnaire.allSelectedAnswerIds)
            }.toMutableList()
        }
    }

    private fun selectAnswers(qwa: QuestionWithAnswers, selectedAnswerIds: List<String>) = qwa.apply {
        if (!question.isMultipleChoice && answers.count { it.id in selectedAnswerIds } > 1) {
            answers.onEach { it.isAnswerSelected = false }
        } else {
            answers.onEach { it.isAnswerSelected = it.id in selectedAnswerIds }
        }
    }


    private fun unsyncNonExistentQuestionnaires(
        syncedCompleteQuestionnaires: List<CompleteQuestionnaire>,
        response: SyncQuestionnairesResponse
    ) = syncedCompleteQuestionnaires
        .filter { response.questionnaireIdsToUnsync.any { id -> id == it.questionnaire.id } }
        .map(CompleteQuestionnaire::questionnaire)
        .onEach { it.syncStatus = SyncStatus.UNSYNCED }


    //TODO -> BULK INSERT/UPDATE OF ALL LOCALLY UNSYNCED QUESTIONNAIRES ONLINE
    //TODO -> Should only upload own Questionnaires
    private suspend fun uploadUnsyncedQuestionnaires(
        unsyncedQuestionnaireIdsProvider: suspend (() -> List<String>) = { localRepository.findAllNonSyncedQuestionnaireIds() }
    ) = withContext(IO) {
        unsyncedQuestionnaireIdsProvider().let { unsyncedQuestionnaireIds ->
            if (unsyncedQuestionnaireIds.isEmpty()) return@withContext

            val userId = preferencesRepository.getUserId()

            runCatching {
                localRepository.findCompleteQuestionnairesWith(unsyncedQuestionnaireIds, userId).map(DataMapper::mapRoomQuestionnaireToMongoQuestionnaire).let {
                    backendRepository.insertQuestionnaires(it)
                }
            }.onSuccess { response ->
                if (response.responseType == InsertQuestionnairesResponseType.SUCCESSFUL) {
                    localRepository.setStatusToSynced(unsyncedQuestionnaireIds)
                }
            }
        }
    }


    /**
     * Sync of locally deleted Questionnaires, so that they will be deleted online
     */
    private suspend fun syncLocallyDeletedQuestionnaires(
        locallyDeletedQuestionnairesProvider: suspend (() -> List<LocallyDeletedQuestionnaire>) = { localRepository.getLocallyDeletedQuestionnaireIds() }
    ) = withContext(IO) {
        locallyDeletedQuestionnairesProvider().let { locallyDeletedQuestionnaires ->
            locallyDeletedQuestionnaires.filter(LocallyDeletedQuestionnaire::isUserOwner).let { questionnaireToDelete ->
                val deleteCreated = async { deleteCreatedQuestionnaires(questionnaireToDelete) }
                val deleteCached = async { deleteCachedQuestionnaires(locallyDeletedQuestionnaires - questionnaireToDelete.toSet()) }
                deleteCreated.await()
                deleteCached.await()
            }
        }
    }

    private suspend fun deleteCreatedQuestionnaires(createdQuestionnaires: List<LocallyDeletedQuestionnaire>) {
        if (createdQuestionnaires.isEmpty()) return

        runCatching {
            backendRepository.deleteQuestionnaire(createdQuestionnaires.map(LocallyDeletedQuestionnaire::questionnaireId))
        }.onSuccess { response ->
            if (response.responseType == DeleteQuestionnaireResponseType.SUCCESSFUL) {
                localRepository.delete(createdQuestionnaires)
            }
        }
    }

    private suspend fun deleteCachedQuestionnaires(cachedQuestionnaires: List<LocallyDeletedQuestionnaire>) {
        if (cachedQuestionnaires.isEmpty()) return

        runCatching {
            backendRepository.deleteFilledQuestionnaire(cachedQuestionnaires.map(LocallyDeletedQuestionnaire::questionnaireId))
        }.onSuccess { response ->
            if (response.responseType == SUCCESSFUL) {
                localRepository.delete(cachedQuestionnaires)
            }
        }
    }


    /**
     * Sync of locally answered Questionnaires, so that they will be inserted online
     */
    private suspend fun syncLocallyAnsweredQuestionnaires(
        unsyncedQuestionnaireIdsProvider: suspend (() -> List<String>) = { localRepository.findAllNonSyncedQuestionnaireIds() }
    ) = withContext(IO) {

        val unsyncedQuestionnaireIds = unsyncedQuestionnaireIdsProvider()

        localRepository.getAllLocallyFilledQuestionnairesToUpload().filter { it.questionnaireId !in unsyncedQuestionnaireIds }.let { filledQuestionnaires ->
            if (filledQuestionnaires.isEmpty()) return@withContext

            runCatching {
                backendRepository.insertFilledQuestionnaires(filledQuestionnaires)
            }.onSuccess { response ->
                if (response.responseType == InsertFilledQuestionnairesResponseType.SUCCESSFUL) {
                    localRepository.delete(filledQuestionnaires.map { LocallyFilledQuestionnaireToUpload(it.questionnaireId) })
                    //TODO -> Noch nicht sicher ob das so gemacht werden muss oder nicht
                    //(filledQuestionnaires.map(MongoFilledQuestionnaire::questionnaireId) - response.notInsertedQuestionnaireIds).map { LocallyFilledQuestionnaireToUpload(it) }.let {
                    //  localRepository.delete(it)
                    //}
                }
            }
        }
    }
}