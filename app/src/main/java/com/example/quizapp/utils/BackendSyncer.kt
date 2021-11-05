package com.example.quizapp.utils

import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.mongodb.documents.questionnairefilled.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.sync.LocallyAnsweredQuestionnaire
import com.example.quizapp.model.databases.room.entities.sync.LocallyClearedQuestionnaire
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedUser
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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendSyncer @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository
) {

    suspend fun syncData() = withContext(IO) {
        val questionnaireAsync = async {
            syncFacultiesAndCoursesOfStudies()
            synAllQuestionnaireData()
        }

        //TODO -> DELETED USER ENTFERNEN UND EINFACH NUR ZULASSEN WENN MAN CONNECTION HAT
        val deletedUserAsync = async {
            syncLocallyDeletedUsers()
        }

        questionnaireAsync.await()
        deletedUserAsync.await()
    }


    private suspend fun syncFacultiesAndCoursesOfStudies() = withContext(IO) {
        val facultiesAsync = getFacultiesAsync()
        val coursesOfStudiesAsync = getCourseOfStudiesAsync()

        localRepository.insert(facultiesAsync.await())

        coursesOfStudiesAsync.await().let { result ->
            localRepository.insert(result.map { it.first })
            localRepository.insert(result.flatMap { it.second })
        }
    }

    private suspend fun getFacultiesAsync() = withContext(IO) {
        async {
            runCatching {
                localRepository.getFacultyIdsWithTimestamp().let { facultiesWithTimestamp ->
                    backendRepository.getFacultySynchronizationData(facultiesWithTimestamp)
                }
            }.onSuccess { response ->
                return@async response.faculties.map(DataMapper::mapMongoFacultyToRoomFaculty)
            }
            return@async emptyList()
        }
    }

    private suspend fun getCourseOfStudiesAsync() = withContext(IO) {
        async {
            runCatching {
                localRepository.getCourseOfStudiesIdsWithTimestamp().let { coursesOfStudiesWithTimestamp ->
                    backendRepository.getCourseOfStudiesSynchronizationData(coursesOfStudiesWithTimestamp)
                }
            }.onSuccess { response ->
                return@async response.coursesOfStudies.map(DataMapper::mapMongoCourseOfStudiesToRoomCourseOfStudies)
            }
            return@async emptyList()
        }
    }

    //TODO -> Subject Einträge kommen von der online Datenbank und werden hier eingetragen
    private suspend fun syncSubject() = withContext(IO) {

    }


    //TODO -> Die müssen auch ignoriert werden beim runterladen von den Antworten
    //TODO -> IGNORE bei finden von den MongoFilledQuestionnaires
    //val locallyDeletedFilledQuestionnaireIds = localRepository.getLocallyDeletedFilledQuestionnaireIds()

    //TODO -> Upload von unsynced Questionnaires erst nachdem die online abgecheckt wurden um zu schauen, welche es online nicht mehr gibt

    //    val uploadUnsyncedQuestionnairesAsync = async {
    //      uploadUnsyncedQuestionnaires {
    //          unsyncedQuestionnaireIdsAsync.await()
    //      }
    //    }
    //    uploadUnsyncedQuestionnairesAsync.await()
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

        val locallyDeletedFilledAsync = async {
            syncLocallyClearedQuestionnaires()
        }

        val locallyAnsweredAsync = async {
            syncLocallyAnsweredQuestionnaires {
                unsyncedQuestionnaireIdsAsync.await()
            }
        }

        syncQuestionnairesAsync.await()
        locallyDeletedAsync.await()
        locallyDeletedFilledAsync.await()
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
                mapToCompleteQuestionnaire(syncedQuestionnaires, response).let {
                    localRepository.insertCompleteQuestionnaires(it)
                }
            }

            val updateAsync = async {
                unsyncNonExistentQuestionnaires(syncedQuestionnaires, response).let {
                    localRepository.update(it)
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


    /**
     * Sync of locally deleted Questionnaires, so that they will be deleted online
     */
    private suspend fun syncLocallyDeletedQuestionnaires(
        locallyDeletedQuestionnairesProvider: suspend (() -> List<LocallyDeletedQuestionnaire>) = { localRepository.getLocallyDeletedQuestionnaireIds() }
    ) = withContext(IO) {
        locallyDeletedQuestionnairesProvider().let { locallyDeletedQuestionnaires ->
            locallyDeletedQuestionnaires.filter(LocallyDeletedQuestionnaire::isUserOwner).let { questionnaireToDelete ->
                val deleteCreated = async { deleteCreatedQuestionnaires(questionnaireToDelete) }
                val deleteCached = async { deleteCachedQuestionnaires(locallyDeletedQuestionnaires - questionnaireToDelete) }
                deleteCreated.await()
                deleteCached.await()
            }
        }
    }

    private suspend fun deleteCreatedQuestionnaires(created: List<LocallyDeletedQuestionnaire>) {
        if (created.isEmpty()) return

        runCatching {
            backendRepository.deleteQuestionnaire(created.map(LocallyDeletedQuestionnaire::questionnaireId))
        }.onSuccess { response ->
            if (response.responseType == DeleteQuestionnaireResponseType.SUCCESSFUL) {
                localRepository.delete(created)
            }
        }
    }

    private suspend fun deleteCachedQuestionnaires(cached: List<LocallyDeletedQuestionnaire>) {
        if (cached.isEmpty()) return

        runCatching {
            backendRepository.deleteFilledQuestionnaire(cached.map(LocallyDeletedQuestionnaire::questionnaireId))
        }.onSuccess { response ->
            if (response.responseType == SUCCESSFUL) {
                localRepository.delete(cached)
            }
        }
    }


    /**
     * Sync of locally deleted Answers of Questionnaires, so that they will be deleted online
     * The Questionnaire itself is still there but the given answers list should be emptied
     */
    private suspend fun syncLocallyClearedQuestionnaires() = withContext(IO) {
        localRepository.getAllLocallyClearedQuestionnaires().let { emptyFilledQuestionnaires ->
            if (emptyFilledQuestionnaires.isEmpty()) return@withContext

            runCatching {
                backendRepository.insertFilledQuestionnaires(emptyFilledQuestionnaires)
            }.onSuccess { response ->
                if (response.responseType == InsertFilledQuestionnairesResponseType.SUCCESSFUL) {
                    localRepository.delete(emptyFilledQuestionnaires.map { LocallyClearedQuestionnaire(it.questionnaireId) })
                }
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

        localRepository.getAllLocallyAnsweredFilledQuestionnaires().filter { it.questionnaireId !in unsyncedQuestionnaireIds }.let { filledQuestionnaires ->
            if (filledQuestionnaires.isEmpty()) return@withContext

            runCatching {
                backendRepository.insertFilledQuestionnaires(filledQuestionnaires)
            }.onSuccess { response ->
                if (response.responseType == InsertFilledQuestionnairesResponseType.SUCCESSFUL) {

                    //TODO -> Noch nicht sicher ob das so gemacht werden muss oder nicht
                    (filledQuestionnaires.map(MongoFilledQuestionnaire::questionnaireId) - response.notInsertedQuestionnaireIds).map { LocallyAnsweredQuestionnaire(it) }.let {
                        localRepository.delete(it)
                    }
                }
            }
        }
    }

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
     * Sync of locally deleted Users -> Should be removed and deletion of Users should be only allowed when it was successful online
     */
    private suspend fun syncLocallyDeletedUsers() = withContext(IO) {
        localRepository.getAllLocallyDeletedUserIds().let { userIds ->
            if (userIds.isEmpty()) return@withContext

            runCatching {
                backendRepository.deleteUsers(userIds.map(LocallyDeletedUser::userId))
            }.onSuccess { response ->
                if (response.responseType == DeleteUserResponseType.SUCCESSFUL) {
                    localRepository.delete(userIds)
                }
            }
        }
    }
}