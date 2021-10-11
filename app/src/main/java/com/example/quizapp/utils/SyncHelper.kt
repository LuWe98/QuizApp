package com.example.quizapp.utils

import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestionsAndAnswers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SyncHelper(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val preferencesRepository: PreferencesRepository
) {

    fun syncData() = applicationScope.launch(Dispatchers.IO) {
        val completeSyncedQuestionnaires = localRepository.findAllSyncedQuestionnaires()
        val locallyDeletedQuestionnaireIds = localRepository.getAllDeletedQuestionnaireIds()
        val unsyncedQuestionnaireIds = localRepository.findAllNonSyncedQuestionnaireIds()

        syncQuestionnaires(completeSyncedQuestionnaires, locallyDeletedQuestionnaireIds, unsyncedQuestionnaireIds)
        syncLocallyDeletedQuestionnaires(locallyDeletedQuestionnaireIds)
        syncDownloadedQuestionnaires()
    }

    private fun syncQuestionnaires(
        syncedQuestionnaires: List<QuestionnaireWithQuestionsAndAnswers>,
        locallyDeletedQuestionnaireIds: List<LocallyDeletedQuestionnaire>,
        unsyncedQuestionnaireIds: List<String>
    ) = applicationScope.launch(Dispatchers.IO) {

        val response = try {
            backendRepository.getQuestionnairesForSyncronization(
                syncedQuestionnaires.map { it.asQuestionnaireIdWithTimestamp },
                unsyncedQuestionnaireIds,
                locallyDeletedQuestionnaireIds
            )
        } catch (e: Exception) {
            null
        } ?: return@launch

        insertAndUpdateQuestionnaires(syncedQuestionnaires, response.mongoQuestionnaires, response.mongoFilledQuestionnaires)
        unsyncQuestionnaire(syncedQuestionnaires, response.questionnaireIdsToUnsync)
    }


    private fun unsyncQuestionnaire(
        syncedQuestionnaires: List<QuestionnaireWithQuestionsAndAnswers>,
        questionnaireIds: List<String>
    ) = applicationScope.launch(Dispatchers.IO) {

        val questionnairesToUpdate = syncedQuestionnaires.filter {
            questionnaireIds.contains(it.questionnaire.id)
        }.map { it.questionnaire }.onEach { it.syncStatus = SyncStatus.UNSYNCED }

        localRepository.update(questionnairesToUpdate)
    }


    private fun insertAndUpdateQuestionnaires(
        syncedQuestionnaires: List<QuestionnaireWithQuestionsAndAnswers>,
        mongoQuestionnaires: List<MongoQuestionnaire>,
        mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>
    ) = applicationScope.launch(Dispatchers.IO) {
        mongoQuestionnaires.map(DataMapper::mapMongoObjectToSqlEntities).apply {

            localRepository.deleteQuestionnairesWith(map { it.questionnaire.id })
            localRepository.insert(map { it.questionnaire })
            localRepository.insert(flatMap { it.allQuestions })
            localRepository.insert(flatMap { it.allAnswers })

            forEach { completeQuestionnaire ->
                applicationScope.launch(Dispatchers.IO) {
                    val answerForQuestionnaire = mongoFilledQuestionnaires.firstOrNull { it.questionnaireId == completeQuestionnaire.questionnaire.id }
                    if (answerForQuestionnaire != null) {
                        //ES GIBT KEINEN LOKALEN FRAGEBOGEN MIT DER ID
                        completeQuestionnaire.allAnswers.filter { answer -> answerForQuestionnaire.isAnswerSelected(answer.id) }.let { answers ->
                            localRepository.update(answers.onEach { answer -> answer.isAnswerSelected = true })
                        }
                    } else {
                        //ES GIBT EINEN LOKALEN FRAGEBOGEN MIT IDS
                        syncedQuestionnaires.firstOrNull { it.questionnaire.id == completeQuestionnaire.questionnaire.id }?.let {
                            it.allAnswers.filter { answer -> it.isAnswerSelected(answer.id) }.let { answers ->
                                localRepository.update(answers.onEach { answer -> answer.isAnswerSelected = true })
                            }
                        }
                    }
                }
            }
        }
    }


    private fun syncLocallyDeletedQuestionnaires(questionnaires: List<LocallyDeletedQuestionnaire>) = applicationScope.launch(Dispatchers.IO) {
        if (questionnaires.isEmpty()) return@launch
        val cached = mutableListOf<LocallyDeletedQuestionnaire>()
        val created = mutableListOf<LocallyDeletedQuestionnaire>()
        questionnaires.forEach { if (it.deleteWholeQuestionnaire) created.add(it) else cached.add(it) }
        deleteCreatedQuestionnaires(created)
        deleteCachedQuestionnaires(cached)
    }

    private fun deleteCreatedQuestionnaires(created: List<LocallyDeletedQuestionnaire>) = applicationScope.launch(Dispatchers.IO) {
        if (created.isEmpty()) return@launch

        val response = try {
            backendRepository.deleteQuestionnaire(created.map { it.questionnaireId })
        } catch (e: Exception) {
            null
        }

        if (response != null && response.isSuccessful) {
            localRepository.delete(created)
        }
    }

    private fun deleteCachedQuestionnaires(cached: List<LocallyDeletedQuestionnaire>) = applicationScope.launch(Dispatchers.IO) {
        if (cached.isEmpty()) return@launch

        val response = try {
            backendRepository.deleteFilledQuestionnaire(preferencesRepository.userCredentialsFlow.first().id, cached.map { it.questionnaireId })
        } catch (e: Exception) {
            null
        }

        if (response != null && response.isSuccessful) {
            localRepository.delete(cached)
        }
    }



    //TODO -> INSERT EMPTY QUESTIONNAIRE IN ORDER FOR IT TO BE DOWNLOADED ON DEVICE CHANGE
    //TODO -> NEUEN REQUEST SCHREIBEN?
    private fun syncDownloadedQuestionnaires() = applicationScope.launch(Dispatchers.IO) {
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
    private fun syncUserData() = applicationScope.launch(Dispatchers.IO) {

    }

    //TODO -> Subjects als Entry in der Tabelle online?
    private fun syncCourseOfStudiesAndFaculties() {

    }
}