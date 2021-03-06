package com.example.quizapp.model.ktor.backendsyncer

import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.mongodb.documents.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.MongoQuestionnaire
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.LocalRepositoryImpl
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.FacultyCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.datastore.PreferenceRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse.*
import com.example.quizapp.model.ktor.BackendResponse.DeleteFilledQuestionnaireResponse.*
import com.example.quizapp.model.ktor.BackendResponse.DeleteQuestionnaireResponse.*
import com.example.quizapp.model.ktor.BackendResponse.InsertFilledQuestionnairesResponse.*
import com.example.quizapp.model.ktor.BackendResponse.InsertQuestionnairesResponse.*
import com.example.quizapp.model.ktor.status.SyncStatus
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

//TODO ->
// Wenn lokal ein unsynced Questionnaire ist:
// Abgleich mit der Remote Datenbank, von dort wird auch der Timestamp zurückgegeben um den den Fragebögen zuzuordnen
// Wenn lokal unsynced und der Timestamp gleich ist mit der online Datenbank dann soll der lokale hochgeladen werden
// Wenn lokal unsynced und der Timestamp kleiner ist als in der online Datenbank dann soll der von der online Datenbank runtergeldaden werden und den lokalen überschreiben
// Wenn lokal unsynced und der Timestamp größer als der in der online Datenbank --> Sollte eigentlich nie vorkommen.
// Das mit dem synceden in Datenbankabfragen machen mit einer großen mapped Querie.

@Singleton
class BackendSyncer @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val dataMapper: DataMapper
) {

    suspend fun syncData() = withContext(IO) {
        val facultyAndCursesOfStudiesAsync = async {
            syncFacultiesAndCoursesOfStudies()
        }

        val questionnaireAsync = async {
            synAllQuestionnaireData()
        }

        Pair(facultyAndCursesOfStudiesAsync.await(), questionnaireAsync.await())
    }

    suspend fun syncFacultiesAndCoursesOfStudies(): SyncFacultyAndCourseOfStudiesResultType = withContext(IO) {
        val facultyAsync = async { getSyncFacultiesResponse() }
        val cosAsync = async { getSyncCourseOfStudiesResponse() }

        facultyAsync.await()?.let { facultyResponse ->
            localRepository.insert(facultyResponse.facultiesToInsert.map(dataMapper::mapMongoFacultyToRoomFaculty))
            localRepository.update(facultyResponse.facultiesToUpdate.map(dataMapper::mapMongoFacultyToRoomFaculty))
            localRepository.deleteFacultiesWith(facultyResponse.facultyIdsToDelete)

            cosAsync.await()?.let { cosResponse ->
                val allLocalFaculties = localRepository.getFacultyIdsWithTimestamp().map(FacultyIdWithTimeStamp::facultyId)
                val facultyCosRelations: MutableList<FacultyCourseOfStudiesRelation>

                (cosResponse.coursesOfStudiesToInsert.map(dataMapper::mapMongoCourseOfStudiesToRoomCourseOfStudies)).let {
                    localRepository.insert(it.map(Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>>::first))
                    facultyCosRelations = it.flatMap(Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>>::second).toMutableList()
                }

                (cosResponse.coursesOfStudiesToUpdate.map(dataMapper::mapMongoCourseOfStudiesToRoomCourseOfStudies)).let {
                    localRepository.update(it.map(Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>>::first))
                    facultyCosRelations += it.flatMap(Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>>::second)
                }

                localRepository.deleteCoursesOfStudiesWith(cosResponse.courseOfStudiesIdsToDelete)

                facultyCosRelations.filter { it.facultyId in allLocalFaculties }.let { filteredFacultyCosRelations ->
                    localRepository.insert(filteredFacultyCosRelations)
                }

                return@withContext if (facultyResponse.isEmpty() && cosResponse.isEmpty()) SyncFacultyAndCourseOfStudiesResultType.BOTH_ALREADY_UP_TO_DATE
                else SyncFacultyAndCourseOfStudiesResultType.BOTH_SYNCED
            }

            return@withContext if (facultyResponse.isEmpty()) SyncFacultyAndCourseOfStudiesResultType.FACULTY_ALREADY_UP_TO_DATE
            else SyncFacultyAndCourseOfStudiesResultType.FACULTY_SYNCED
        }

        return@withContext SyncFacultyAndCourseOfStudiesResultType.SYNC_UNSUCCESSFUL
    }

    private suspend fun getSyncFacultiesResponse() = try {
        backendRepository.facultyApi.getFacultySynchronizationData(localRepository.getFacultyIdsWithTimestamp())
    } catch (e: Exception) {
        null
    }

    private suspend fun getSyncCourseOfStudiesResponse() = try {
        backendRepository.courseOfStudiesApi.getCourseOfStudiesSynchronizationData(localRepository.getCourseOfStudiesIdsWithTimestamp())
    } catch (e: Exception) {
        null
    }


    //Die müssen auch ignoriert werden beim runterladen von den Antworten
    //IGNORE bei finden von den MongoFilledQuestionnaires
    //val locallyDeletedFilledQuestionnaireIds = localRepository.getLocallyDeletedFilledQuestionnaireIds()
    /**
     * returns if the requests were successful or not to display something to the user.
     * At the moment the called methods return Boolean values for simplicity. Everything has to be successful in order to count as synced
     */
    suspend fun synAllQuestionnaireData(): SyncQuestionnaireResultType = withContext(IO) {
        localRepository.unsyncAllSyncingQuestionnaires()

        val unsyncedQuestionnaireIdsAsync = async {
            localRepository.findAllUnsyncedQuestionnaireIds()
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

        val syncLocallyDeletedQuestionnairesAsync = async {
            syncLocallyDeletedQuestionnaires {
                locallyDeletedQuestionnaireIdsAsync.await()
            }
        }

        val syncLocallyAnsweredQuestionnairesAsync = async {
            syncLocallyAnsweredQuestionnaires {
                unsyncedQuestionnaireIdsAsync.await()
            }
        }

        if (syncQuestionnairesAsync.await()
            && syncLocallyDeletedQuestionnairesAsync.await()
            && syncLocallyAnsweredQuestionnairesAsync.await()
        ) {
            return@withContext SyncQuestionnaireResultType.DATA_SYNCED
        }
        return@withContext SyncQuestionnaireResultType.NOT_SUCCESSFUL
    }

    private suspend fun syncQuestionnaires(
        locallyDeletedQuestionnairesProvider: suspend (() -> List<LocallyDeletedQuestionnaire>) = { localRepository.getLocallyDeletedQuestionnaireIds() },
        unsyncedQuestionnaireIdsProvider: suspend (() -> List<String>) = { localRepository.findAllUnsyncedQuestionnaireIds() }
    ): Boolean = withContext(IO) {
        val syncedQuestionnaires = localRepository.findAllSyncedQuestionnaires()

        runCatching {
            backendRepository.questionnaireApi.getQuestionnairesForSyncronization(
                syncedQuestionnaires.map(CompleteQuestionnaire::asQuestionnaireIdWithTimestamp),
                unsyncedQuestionnaireIdsProvider(),
                locallyDeletedQuestionnairesProvider()
            )
        }.onSuccess { response ->
            val insertAsync = async {
                mapToCompleteQuestionnaire(syncedQuestionnaires, response).let { questionnairesToInsert ->
                    localRepository.insertCompleteQuestionnaires(questionnairesToInsert)
                }

                response.mongoQuestionnaires.flatMap(dataMapper::mapMongoQuestionnaireToRoomQuestionnaireCourseOfStudiesRelation).let {
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

            return@withContext uploadUnsyncedQuestionnaires()
        }

        return@withContext false
    }

    private fun mapToCompleteQuestionnaire(
        syncedQuestionnaires: List<CompleteQuestionnaire>,
        response: SyncQuestionnairesResponse
    ) = response.mongoQuestionnaires.map(dataMapper::mapMongoQuestionnaireToRoomCompleteQuestionnaire).onEach { cQ ->
        val selectedAnswerIds = response.mongoFilledQuestionnaires.firstOrNull { it.questionnaireId == cQ.questionnaire.id }?.allSelectedAnswerIds
            ?: syncedQuestionnaires.firstOrNull { it.questionnaire.id == cQ.questionnaire.id }?.allSelectedAnswerIds

        selectedAnswerIds?.let {
            cQ.questionsWithAnswers = cQ.questionsWithAnswers.map { qwa ->
                qwa.apply {
                    answers = if (!question.isMultipleChoice && answers.count { it.id in selectedAnswerIds } > 1) {
                        answers.map { it.copy(isAnswerSelected = false) }
                    } else {
                        answers.map { it.copy(isAnswerSelected = it.id in selectedAnswerIds) }
                    }
                }
            }
        }
    }

    private fun mapToCompleteQuestionnaire2(
        syncedQuestionnaires: List<CompleteQuestionnaire>,
        receivedMongoQuestionnaires: List<MongoQuestionnaire>
    ) {
        val mappedReceived = receivedMongoQuestionnaires.map(dataMapper::mapMongoQuestionnaireToRoomCompleteQuestionnaire)

        mappedReceived.onEach { cQ ->
            val selectedAnswerIds = mappedReceived.firstOrNull { it.questionnaire.id == cQ.questionnaire.id }?.allSelectedAnswerIds
                ?: syncedQuestionnaires.firstOrNull { it.questionnaire.id == cQ.questionnaire.id }?.allSelectedAnswerIds

            selectedAnswerIds?.let {
                cQ.questionsWithAnswers = cQ.questionsWithAnswers.map { qwa ->
                    qwa.apply {
                        answers = if (!question.isMultipleChoice && answers.count { it.id in selectedAnswerIds } > 1) {
                            answers.map { it.copy(isAnswerSelected = false) }
                        } else {
                            answers.map { it.copy(isAnswerSelected = it.id in selectedAnswerIds) }
                        }
                    }
                }
            }
        }
    }


    private fun unsyncNonExistentQuestionnaires(
        syncedCompleteQuestionnaires: List<CompleteQuestionnaire>,
        response: SyncQuestionnairesResponse
    ) = syncedCompleteQuestionnaires
        .filter { response.questionnaireIdsToUnsync.any { id -> id == it.questionnaire.id } }
        .map(CompleteQuestionnaire::questionnaire)
        .map { it.copy(syncStatus = SyncStatus.UNSYNCED) }


    private suspend fun uploadUnsyncedQuestionnaires(
        unsyncedQuestionnaireIdsProvider: suspend (() -> List<String>) = { localRepository.findAllUnsyncedQuestionnaireIds() }
    ): Boolean = withContext(IO) {
        unsyncedQuestionnaireIdsProvider().let { unsyncedQuestionnaireIds ->
            if (unsyncedQuestionnaireIds.isEmpty()) return@withContext true

            val mongoQuestionnaires = localRepository.findCompleteQuestionnairesWith(
                unsyncedQuestionnaireIds,
                preferenceRepository.getUserId()
            ).map(dataMapper::mapRoomQuestionnaireToMongoQuestionnaire)

            if (mongoQuestionnaires.isEmpty()) return@withContext true

            runCatching {
                backendRepository.questionnaireApi.insertQuestionnaires(mongoQuestionnaires)
            }.onSuccess { response ->
                if (response.responseType == InsertQuestionnairesResponseType.SUCCESSFUL) {
                    localRepository.setStatusToSynced(unsyncedQuestionnaireIds)
                    return@withContext true
                }
            }
            return@withContext false
        }
    }


    /**
     * Sync of locally deleted Questionnaires, so that they will be deleted online
     */
    private suspend fun syncLocallyDeletedQuestionnaires(
        locallyDeletedQuestionnairesProvider: suspend (() -> List<LocallyDeletedQuestionnaire>) = { localRepository.getLocallyDeletedQuestionnaireIds() }
    ): Boolean = withContext(IO) {
        locallyDeletedQuestionnairesProvider().let { locallyDeletedQuestionnaires ->
            locallyDeletedQuestionnaires.filter(LocallyDeletedQuestionnaire::isUserOwner).let { questionnaireToDelete ->

                val deleteCreated = async {
                    deleteCreatedQuestionnaires(questionnaireToDelete)
                }

                val deleteCached = async {
                    deleteCachedQuestionnaires(locallyDeletedQuestionnaires - questionnaireToDelete.toSet())
                }

                return@withContext deleteCreated.await() && deleteCached.await()
            }
        }
    }

    private suspend fun deleteCreatedQuestionnaires(createdQuestionnaires: List<LocallyDeletedQuestionnaire>): Boolean {
        if (createdQuestionnaires.isEmpty()) return true

        runCatching {
            backendRepository.questionnaireApi.deleteQuestionnaire(createdQuestionnaires.map(LocallyDeletedQuestionnaire::questionnaireId))
        }.onSuccess { response ->
            if (response.responseType == DeleteQuestionnaireResponseType.SUCCESSFUL) {
                localRepository.delete(createdQuestionnaires)
                return true
            }
        }
        return false
    }

    private suspend fun deleteCachedQuestionnaires(cachedQuestionnaires: List<LocallyDeletedQuestionnaire>): Boolean {
        if (cachedQuestionnaires.isEmpty()) return true

        runCatching {
            backendRepository.filledQuestionnaireApi.deleteFilledQuestionnaire(cachedQuestionnaires.map(LocallyDeletedQuestionnaire::questionnaireId))
        }.onSuccess { response ->
            if (response.responseType == DeleteFilledQuestionnaireResponseType.SUCCESSFUL) {
                localRepository.delete(cachedQuestionnaires)
                return true
            }
        }
        return false
    }


    /**
     * Sync of locally answered Questionnaires, so that they will be inserted online
     */
    private suspend fun syncLocallyAnsweredQuestionnaires(
        unsyncedQuestionnaireIdsProvider: suspend (() -> List<String>) = { localRepository.findAllUnsyncedQuestionnaireIds() }
    ): Boolean = withContext(IO) {

        val unsyncedQuestionnaireIds = unsyncedQuestionnaireIdsProvider()

        localRepository.getLocallyAnsweredCompleteQuestionnaires()
            .map(dataMapper::mapRoomQuestionnaireToMongoFilledQuestionnaire)
            .filter { it.questionnaireId !in unsyncedQuestionnaireIds }.let { filledQuestionnaires ->
                if (filledQuestionnaires.isEmpty()) return@withContext true

                runCatching {
                    backendRepository.filledQuestionnaireApi.insertFilledQuestionnaires(filledQuestionnaires)
                }.onSuccess { response ->
                    if (response.responseType == InsertFilledQuestionnairesResponseType.SUCCESSFUL) {
                        localRepository.delete(filledQuestionnaires.map(MongoFilledQuestionnaire::asLocallyFilledQuestionnaireToUpload))
                        //TODO -> Noch nicht sicher ob das so gemacht werden muss oder nicht
                        //(filledQuestionnaires.map(MongoFilledQuestionnaire::questionnaireId) - response.notInsertedQuestionnaireIds).map { LocallyFilledQuestionnaireToUpload(it) }.let {
                        //  localRepository.delete(it)
                        //}
                        return@withContext true
                    }
                }
                return@withContext false
            }
    }
}