package com.example.quizapp.utils

import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.SyncQuestionnairesResponse
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.sync.LocallyAnsweredQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

//TODO -> SYNCDING MIT SWIPE REFRESH ANSCHAUEN!
class BackendSyncHelper(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val preferencesRepository: PreferencesRepository
) {

    suspend fun syncData() = withContext(IO) {
        val questionnaireAsync = async { synAllQuestionnaireData() }
        val userAsync = async { syncUserData() }
        val test = async { syncLocallyAnsweredQuestionnaires() }

        questionnaireAsync.await()
        userAsync.await()
        test.await()
    }

    suspend fun synAllQuestionnaireData() = withContext(IO) {
        val locallyDeletedQuestionnaireIds = localRepository.getLocallyDeletedQuestionnaireIds()

        //TODO -> Die müssen auch ignoriert werden beim runterladen von den Antorten
        //TODO -> IGNORE bei finden von den MongoFilledQuestionnaires
        val locallyDeletedFilledQuestionnaireIds = localRepository.getLocallyDeletedFilledQuestionnaireIds()

        val syncAsync = async { syncQuestionnaires { locallyDeletedQuestionnaireIds } }
        val locallyAsync = async { syncLocallyDeletedQuestionnaires(locallyDeletedQuestionnaireIds) }
        val downloadAsync = async { syncDownloadedQuestionnaires() }

        syncAsync.await()
        locallyAsync.await()
        downloadAsync.await()
    }

    private suspend fun syncQuestionnaires(
        locallyDeletedQuestionnaireIds: suspend () -> List<LocallyDeletedQuestionnaire> = { localRepository.getLocallyDeletedQuestionnaireIds() }
    ) = withContext(IO) {
        val syncedQuestionnaires = localRepository.findAllSyncedQuestionnaires()
        val unsyncedQuestionnaireIds = localRepository.findAllNonSyncedQuestionnaireIds()

        val response = try {
            backendRepository.getQuestionnairesForSyncronization(
                syncedQuestionnaires.map { it.asQuestionnaireIdWithTimestamp },
                unsyncedQuestionnaireIds,
                locallyDeletedQuestionnaireIds.invoke()
            )
        } catch (e: Exception) {
            null
        } ?: return@withContext

        val insertAsync = async { localRepository.insertCompleteQuestionnaires(mapToCompleteQuestionnaire(syncedQuestionnaires, response)) }
        val updateAsync = async { localRepository.update(unsyncNonExistentQuestionnaires(syncedQuestionnaires, response)) }

        insertAsync.await()
        updateAsync.await()
    }

    private fun mapToCompleteQuestionnaire(
        syncedCompleteQuestionnaires: List<CompleteQuestionnaireJunction>,
        response: SyncQuestionnairesResponse
    ) = response.mongoQuestionnaires.map(DataMapper::mapMongoObjectToSqlEntities).onEach { q ->
        response.mongoFilledQuestionnaires.firstOrNull { it.questionnaireId == q.questionnaire.id }?.let { remoteFilledQuestionnaire ->
            q.allAnswers.onEach { answer -> answer.isAnswerSelected = remoteFilledQuestionnaire.isAnswerSelected(answer.id) }
        } ?: syncedCompleteQuestionnaires.firstOrNull { it.questionnaire.id == q.questionnaire.id }?.let { locallySyncedQuestionnaire ->
            q.allAnswers.onEach { answer -> answer.isAnswerSelected = locallySyncedQuestionnaire.isAnswerSelected(answer.id) }
        }
    }

    private fun unsyncNonExistentQuestionnaires(
        syncedCompleteQuestionnaires: List<CompleteQuestionnaireJunction>,
        response: SyncQuestionnairesResponse
    ) = syncedCompleteQuestionnaires
        .filter { response.questionnaireIdsToUnsync.any { id -> id == it.questionnaire.id } }
        .map { it.questionnaire }
        .onEach { it.syncStatus = SyncStatus.UNSYNCED }



    //TODO -> BULK INSERT/UPDATE OF ALL LOCALLY UNSYNCED QUESTIONNAIRES ONLINE
    private suspend fun uploadUnsyncedQuestionnaires(){

    }


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
            if(response.isSuccessful){
                localRepository.delete(created)
            }
        }
    }

    private suspend fun deleteCachedQuestionnaires(cached: List<LocallyDeletedQuestionnaire>) {
        if (cached.isEmpty()) return

        runCatching {
            backendRepository.deleteFilledQuestionnaire(preferencesRepository.userInfoFlow.first().id, cached.map { it.questionnaireId })
        }.onSuccess { response ->
            if(response.isSuccessful){
                localRepository.delete(cached)
            }
        }
    }



    //TODO -> Sync Locally Deleted Answers for Questionnaire
    private suspend fun syncLocallyDeletedFilledQuestionnaires() = withContext(IO) {

    }


    private suspend fun syncLocallyAnsweredQuestionnaires() = withContext(IO) {
        localRepository.getAllLocallyAnsweredFilledQuestionnaires().let { filledQuestionnaires ->
            if(filledQuestionnaires.isEmpty()) return@withContext

            runCatching {
                backendRepository.insertFilledQuestionnaires(filledQuestionnaires)
            }.onSuccess { response ->
                if(response.isSuccessful){
                    (filledQuestionnaires.map { it.questionnaireId } - response.notInsertedQuestionnaireIds).map { LocallyAnsweredQuestionnaire(it) }.let {
                        localRepository.delete(it)
                    }
                }
            }
        }
    }








    //TODO -> INSERT EMPTY QUESTIONNAIRE IN ORDER FOR IT TO BE DOWNLOADED ON DEVICE CHANGE
    //TODO -> NEUEN REQUEST SCHREIBEN?
    private suspend fun syncDownloadedQuestionnaires()  {
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

    //TODO -> User Info validieren und gegebenenfalls username oder Role updaten
    private suspend fun syncUserData() {

    }

    //TODO -> Subjects als Entry in der Tabelle online?
    private suspend fun syncCourseOfStudiesAndFaculties() {

    }



    //        insertOrUpdateQuestionnaires(syncedQuestionnaires, response.mongoQuestionnaires, response.mongoFilledQuestionnaires)

//    private fun insertOrUpdateQuestionnaires(
//        syncedQuestionnaires: List<QuestionnaireWithQuestionsAndAnswers>,
//        mongoQuestionnaires: List<MongoQuestionnaire>,
//        mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>
//    ) = applicationScope.launch(Dispatchers.IO) {
//        mongoQuestionnaires.map(DataMapper::mapMongoObjectToSqlEntities).let { questionnaires ->
//
//            localRepository.insertCompleteQuestionnaires(questionnaires)
//
//            questionnaires.forEach { completeQuestionnaire ->
//                insertAnswers(completeQuestionnaire, mongoFilledQuestionnaires, syncedQuestionnaires)
//            }
//        }
//    }
//
//
//    private fun insertAnswers(
//        completeQuestionnaire: QuestionnaireWithQuestionsAndAnswers,
//        mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>,
//        syncedQuestionnaires: List<QuestionnaireWithQuestionsAndAnswers>
//    ) = applicationScope.launch(Dispatchers.IO) {
//        mongoFilledQuestionnaires.firstOrNull { it.questionnaireId == completeQuestionnaire.questionnaire.id }?.let { remoteFilledQuestionnaire ->
//            updateAnswers(completeQuestionnaire) { answer -> remoteFilledQuestionnaire.isAnswerSelected(answer.id) }
//        } ?: syncedQuestionnaires.firstOrNull { it.questionnaire.id == completeQuestionnaire.questionnaire.id }?.let { locallySyncedQuestionnaire ->
//            updateAnswers(locallySyncedQuestionnaire) { answer -> locallySyncedQuestionnaire.isAnswerSelected(answer.id) }
//        }
//    }
//
//    private suspend fun updateAnswers(completeQuestionnaire: QuestionnaireWithQuestionsAndAnswers, answerFilter: (Answer) -> (Boolean)) {
//        completeQuestionnaire.allAnswers.filter(answerFilter).let { answers ->
//            localRepository.update(answers.onEach { answer -> answer.isAnswerSelected = true })
//        }
//    }
}