package com.example.quizapp.utils

import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.SyncQuestionnairesResponse
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.sync.LocallyAnsweredQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDeletedFilledQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import com.example.quizapp.model.room.junctions.QuestionWithAnswers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

//TODO -> Main TODO ist, dass wenn man einen Fragebogen updated und multiple choice in single choice umbaut, dass maximal eine antwort selected ist
//TODO -> Soll dann überhaupt noch eine Frage slected sein oder lieber alle deselecten?
//TODO -> Vllt timestamp auf den Fragen, wann diese bearbeitet wurden? Wenn Timestamp abweicht werden gegebene Antworten gelöscht / nicht gewertet
class BackendSyncer(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val preferencesRepository: PreferencesRepository
) {

    suspend fun syncData() = withContext(IO) {
        val questionnaireAsync = async { synAllQuestionnaireData() }
        val userInfoAsync = async { syncUserData() }
        val deletedAsync = async { syncLocallyDeletedUsers() }

        questionnaireAsync.await()
        userInfoAsync.await()
        deletedAsync.await()
    }

    //TODO -> Die müssen auch ignoriert werden beim runterladen von den Antorten
    //TODO -> IGNORE bei finden von den MongoFilledQuestionnaires
    //val locallyDeletedFilledQuestionnaireIds = localRepository.getLocallyDeletedFilledQuestionnaireIds()

    suspend fun synAllQuestionnaireData() = withContext(IO) {
        localRepository.unsyncAllSyncingQuestionnaires()
        val locallyDeletedQuestionnaireIdsAsync = async { localRepository.getLocallyDeletedQuestionnaireIds() }

        val syncQuestionnairesAsync = async { syncQuestionnaires(locallyDeletedQuestionnaireIdsAsync.await()) }
        val locallyDeletedAsync = async { syncLocallyDeletedQuestionnaires(locallyDeletedQuestionnaireIdsAsync.await()) }
        val locallyDeletedFilledAsync = async { syncLocallyDeletedFilledQuestionnaires() }
        val locallyAnsweredAsync = async { syncLocallyAnsweredQuestionnaires() }
        val locallyDownloadAsync = async { syncLocallyDownloadedQuestionnaires() }

        syncQuestionnairesAsync.await()
        locallyDeletedAsync.await()
        locallyDeletedFilledAsync.await()
        locallyAnsweredAsync.await()
        locallyDownloadAsync.await()
    }

    private suspend fun syncQuestionnaires(
        locallyDeletedQuestionnaireIds: List<LocallyDeletedQuestionnaire>
    ) = withContext(IO) {
        val syncedQuestionnaires = async { localRepository.findAllSyncedQuestionnaires() }
        val unsyncedQuestionnaireIds = async { localRepository.findAllNonSyncedQuestionnaireIds() }

        val response = try {
            backendRepository.getQuestionnairesForSyncronization(
                syncedQuestionnaires.await().map(CompleteQuestionnaireJunction::asQuestionnaireIdWithTimestamp),
                unsyncedQuestionnaireIds.await(),
                locallyDeletedQuestionnaireIds
            )
        } catch (e: Exception) {
            null
        } ?: return@withContext

        val insertAsync = async {
            mapToCompleteQuestionnaire(syncedQuestionnaires.await(), response).let {
                localRepository.insertCompleteQuestionnaires(it)
            }
        }

        val updateAsync = async {
            unsyncNonExistentQuestionnaires(syncedQuestionnaires.await(), response).let {
                localRepository.update(it)
            }
        }

        insertAsync.await()
        updateAsync.await()
    }

    private fun mapToCompleteQuestionnaire(
        syncedCompleteQuestionnaires: List<CompleteQuestionnaireJunction>,
        response: SyncQuestionnairesResponse
    ) = response.mongoQuestionnaires.map(DataMapper::mapMongoObjectToSqlEntities).onEach { q ->
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
        if (!question.isMultipleChoice && answers.count { answer -> selectedAnswerIds.contains(answer.id) } > 1) {
            answers.onEach { answer -> answer.isAnswerSelected = false }
        } else {
            answers.onEach { answer -> answer.isAnswerSelected = selectedAnswerIds.contains(answer.id) }
        }
    }


    private fun unsyncNonExistentQuestionnaires(
        syncedCompleteQuestionnaires: List<CompleteQuestionnaireJunction>,
        response: SyncQuestionnairesResponse
    ) = syncedCompleteQuestionnaires
        .filter { response.questionnaireIdsToUnsync.any { id -> id == it.questionnaire.id } }
        .map(CompleteQuestionnaireJunction::questionnaire)
        .onEach { it.syncStatus = SyncStatus.UNSYNCED }


    /**
     * Sync of locally deleted Questionnaires, so that they will be deleted online
     */
    private suspend fun syncLocallyDeletedQuestionnaires(questionnaires: List<LocallyDeletedQuestionnaire>) = withContext(IO) {
        questionnaires.filter { it.isUserOwner }.let { questionnaireToDelete ->
            val deleteCreated = async { deleteCreatedQuestionnaires(questionnaireToDelete) }
            val deleteCached = async { deleteCachedQuestionnaires(questionnaires - questionnaireToDelete) }
            deleteCreated.await()
            deleteCached.await()
        }
    }

    private suspend fun deleteCreatedQuestionnaires(created: List<LocallyDeletedQuestionnaire>) {
        if (created.isEmpty()) return

        runCatching {
            backendRepository.deleteQuestionnaire(created.map { it.questionnaireId })
        }.onSuccess { response ->
            if (response.isSuccessful) {
                localRepository.delete(created)
            }
        }
    }

    private suspend fun deleteCachedQuestionnaires(cached: List<LocallyDeletedQuestionnaire>) {
        if (cached.isEmpty()) return

        runCatching {
            backendRepository.deleteFilledQuestionnaire(cached.map { it.questionnaireId })
        }.onSuccess { response ->
            if (response.isSuccessful) {
                localRepository.delete(cached)
            }
        }
    }


    /**
     * Sync of locally deleted Answers of Questionnaires, so that they will be deleted online
     */
    private suspend fun syncLocallyDeletedFilledQuestionnaires() = withContext(IO) {
        localRepository.getAllLocallyDeletedFilledQuestionnaires().let { emptyFilledQuestionnaires ->
            if (emptyFilledQuestionnaires.isEmpty()) return@withContext

            runCatching {
                backendRepository.insertFilledQuestionnaires(emptyFilledQuestionnaires)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    localRepository.delete(emptyFilledQuestionnaires.map { LocallyDeletedFilledQuestionnaire(it.questionnaireId) })
                }
            }
        }
    }


    /**
     * Sync of locally answered Questionnaires, so that they will be inserted online
     */
    private suspend fun syncLocallyAnsweredQuestionnaires() = withContext(IO) {
        localRepository.getAllLocallyAnsweredFilledQuestionnaires().let { filledQuestionnaires ->
            if (filledQuestionnaires.isEmpty()) return@withContext

            runCatching {
                backendRepository.insertFilledQuestionnaires(filledQuestionnaires)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    //TODO -> Noch nicht sicher ob das so gemacht werden muss oder nicht
                    (filledQuestionnaires.map { it.questionnaireId } - response.notInsertedQuestionnaireIds).map { LocallyAnsweredQuestionnaire(it) }.let {
                        localRepository.delete(it)
                    }
                }
            }
        }
    }


    /**
     * Sync of locally downloaded Questionnaires, so that an empty MongoFilledQuestionnaire will be inserted for device change
     */
    private suspend fun syncLocallyDownloadedQuestionnaires() = withContext(IO) {
        localRepository.getAllLocallyDownloadedQuestionnaireIds().let {
//            val response = try {
//                backendRepository.insertEmptyFilledQuestionnaire(
//                    MongoFilledQuestionnaire(
//                        questionnaireId = questionnaireId,
//                        userId = preferencesRepository.userCredentialsFlow.first().id
//                    )
//                )
//            } catch (e: Exception) {
//                localRepository.insert(LocallyDownloadedQuestionnaire(questionnaireId))
//                null
//            }
        }
    }


    private suspend fun syncLocallyDeletedUsers() = withContext(IO) {
        localRepository.getAllLocallyDeletedUserIds().let { userIds ->
            if(userIds.isEmpty()) return@withContext

            runCatching {
                backendRepository.deleteUsers(userIds.map { it.userId })
            }.onSuccess { response ->
                if(response.isSuccessful){
                    localRepository.delete(userIds)
                }
            }
        }
    }



    //TODO -> BULK INSERT/UPDATE OF ALL LOCALLY UNSYNCED QUESTIONNAIRES ONLINE
    private suspend fun uploadUnsyncedQuestionnaires() {

    }


    //TODO -> User Info validieren und gegebenenfalls username oder Role updaten
    private suspend fun syncUserData() {

    }


    //TODO -> CourseOfStudies Einträge kommen von der online Datenbank und werden hier eingetragen
    private suspend fun syncCourseOfStudies() {

    }

    //TODO -> Subject Einträge kommen von der online Datenbank und werden hier eingetragen
    private suspend fun syncSubject() {

    }

    //TODO -> Faculty Einträge kommen von der online Datenbank und werden hier eingetragen
    private suspend fun syncFaculties() {

    }
}