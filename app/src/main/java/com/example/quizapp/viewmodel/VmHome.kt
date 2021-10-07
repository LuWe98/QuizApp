package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.first
import com.example.quizapp.extensions.log
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.ktor.responses.BackendResponse.*
import com.example.quizapp.model.ktor.responses.BackendResponse.InsertFilledQuestionnaireResponse.*
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.SyncStatus
import com.example.quizapp.model.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.entities.LocallyDownloadedQuestionnaire
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestionsAndAnswers
import com.example.quizapp.viewmodel.VmHome.FragmentHomeEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VmHome @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository
) : ViewModel() {

    init {
        syncData()
    }

    private val userId = preferencesRepository.userCredentialsFlow.first(viewModelScope).id

    private val fragmentHomeEventChannel = Channel<FragmentHomeEvent>()

    val fragmentHomeEventChannelFlow get() = fragmentHomeEventChannel.receiveAsFlow()

    val allQuestionnairesWithQuestionsLD = localRepository.findAllQuestionnairesWithQuestionsNotForUserFlow(userId).asLiveData().distinctUntilChanged()

    val allQuestionnairesWithQuestionsForUserLD = localRepository.findAllQuestionnairesWithQuestionsForUserFlow(userId).asLiveData().distinctUntilChanged()

    val allQuestionnairesFromDatabase = flow {
        try {
            emit(backendRepository.getAllQuestionnaires())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO).asLiveData().distinctUntilChanged()



    fun onCachedItemDownLoadButtonClicked(mongoQuestionnaire: MongoQuestionnaire) {
        applicationScope.launch(Dispatchers.IO) {
            DataMapper.mapMongoObjectToSqlEntities(mongoQuestionnaire).apply {
                localRepository.insert(questionnaire)
                localRepository.insert(allQuestions)
                localRepository.insert(allAnswers)
                uploadEmptyQuestionnaire(questionnaire.id)
            }
        }
    }

    fun onCreatedItemSyncButtonClicked(questionnaireId: String) {
        applicationScope.launch(Dispatchers.IO) {
            val completeQuestionnaire = localRepository.findCompleteQuestionnaireWith(questionnaireId)
            localRepository.update(completeQuestionnaire.questionnaire.apply { syncStatus = SyncStatus.SYNCING })

            val result = try {
                backendRepository.insertQuestionnaire(completeQuestionnaire)
            } catch (e: Exception) {
                null
            }

            if (result != null && result.isSuccessful) {
                localRepository.update(completeQuestionnaire.questionnaire.apply { syncStatus = SyncStatus.SYNCED })
                fragmentHomeEventChannel.send(ShowSnackBarMessageBar(R.string.syncSuccessful))
            } else {
                localRepository.update(completeQuestionnaire.questionnaire.apply { syncStatus = SyncStatus.UNSYNCED })
                fragmentHomeEventChannel.send(ShowSnackBarMessageBar(R.string.syncUnsuccessful))
            }
        }
    }


    //TODO -> UNDO BUTTON in SNACKBAR HINZUFÜGEN UM DELETION RÜCKGÄNGIG ZU MACHEN!
    fun onCachedItemDeleteQuestionnaireClicked(questionnaire: Questionnaire) {
        applicationScope.launch(Dispatchers.IO) {
            localRepository.deleteQuestionnaireWith(questionnaire.id)
            localRepository.delete(LocallyDownloadedQuestionnaire(questionnaire.id))

            val response = try {
                backendRepository.deleteFilledQuestionnaire(preferencesRepository.userCredentialsFlow.first().id, listOf(questionnaire.id))
            } catch (e: Exception) {
                null
            }

            if (response == null || !response.isSuccessful) {
                localRepository.insert(LocallyDeletedQuestionnaire(questionnaire.id, false))
            }
        }
    }

    fun onCreatedItemDeleteQuestionnaireClicked(questionnaire: Questionnaire) {
        applicationScope.launch(Dispatchers.IO) {
            localRepository.deleteQuestionnaireWith(questionnaire.id)

            val response = try {
                backendRepository.deleteQuestionnaire(listOf(questionnaire.id))
            } catch (e: Exception) {
                null
            }

            if (response == null || !response.isSuccessful) {
                localRepository.insert(LocallyDeletedQuestionnaire(questionnaire.id, true))
            }
        }
    }



    //    log("RESPONSE ${response.mongoQuestionnaires}")
    //    log("ANSWERS: ${response.mongoFilledQuestionnaires}")
    //TODO -> Checken welche Fragebögen sich wie verändert haben und anschließend updaten!
    private fun syncData() = applicationScope.launch(Dispatchers.IO) {
        val completeSyncedQuestionnaires = localRepository.findAllSyncedQuestionnaires()
        val locallyDeletedQuestionnaireIds = localRepository.getAllDeletedQuestionnaireIds()
        val unsyncedQuestionnaireIds = localRepository.findAllNonSyncedQuestionnaireIds()

        syncLocallyDeletedQuestionnaires(locallyDeletedQuestionnaireIds)
        syncDownloadedQuestionnaires()
        syncQuestionnaires(completeSyncedQuestionnaires, locallyDeletedQuestionnaireIds, unsyncedQuestionnaireIds)
    }

    private fun syncQuestionnaires(
        completeSyncedQuestionnaires: List<QuestionnaireWithQuestionsAndAnswers>,
        locallyDeletedQuestionnaireIds: List<LocallyDeletedQuestionnaire>,
        unsyncedQuestionnaireIds: List<String>
    ) = applicationScope.launch(Dispatchers.IO) {

        val response = try {
            backendRepository.getQuestionnairesForSyncronization(
                completeSyncedQuestionnaires.map { it.asQuestionnaireIdWithTimestamp },
                unsyncedQuestionnaireIds,
                locallyDeletedQuestionnaireIds
            )
        } catch (e: Exception) {
            null
        } ?: return@launch

        applicationScope.launch(Dispatchers.IO) {
            response.mongoQuestionnaires.map { DataMapper.mapMongoObjectToSqlEntities(it) }.apply {
                localRepository.deleteQuestionnairesWith(map { it.questionnaire.id })
                localRepository.insert(map { it.questionnaire })
                localRepository.insert(flatMap { it.allQuestions })
                localRepository.insert(flatMap { it.allAnswers })

                forEach { completeQuestionnaire ->
                    applicationScope.launch(Dispatchers.IO) {
                        val first = response.mongoFilledQuestionnaires.firstOrNull { it.questionnaireId == completeQuestionnaire.questionnaire.id }
                        if (first != null) {
                            //ES GIBT KEINEN LOKALEN FRAGEBOGEN MIT DER ID
                            completeQuestionnaire.allAnswers.filter { answer -> first.isAnswerSelected(answer.id) }.let { answers ->
                                localRepository.update(answers.onEach { answer -> answer.isAnswerSelected = true })
                            }
                        } else {
                            //ES GIBT EINEN LOKALEN FRAGEBOGEN MIT IDS
                            completeSyncedQuestionnaires.firstOrNull { it.questionnaire.id == completeQuestionnaire.questionnaire.id }?.let {
                                it.allAnswers.filter { answer -> it.isAnswerSelected(answer.id) }.let { answers ->
                                    localRepository.update(answers.onEach { answer -> answer.isAnswerSelected = true })
                                }
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
        questionnaires.forEach {
            if (it.deleteWholeQuestionnaire) created.add(it) else cached.add(it)
        }

        applicationScope.launch(Dispatchers.IO) createdLaunch@{
            if (created.isEmpty()) return@createdLaunch

            val response = try {
                backendRepository.deleteQuestionnaire(created.map { it.questionnaireId })
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                localRepository.delete(created)
            }
        }

        applicationScope.launch(Dispatchers.IO) createdLaunch@{
            if (cached.isEmpty()) return@createdLaunch

            val response = try {
                backendRepository.deleteFilledQuestionnaire(preferencesRepository.userCredentialsFlow.first().id, cached.map { it.questionnaireId })
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                localRepository.delete(cached)
            }
        }
    }


    //TODO -> INSERT EMPTY QUESTIONNAIRE IN ORDER FOR IT TO BE DOWNLOADED
    private fun syncDownloadedQuestionnaires() = applicationScope.launch(Dispatchers.IO) {
        localRepository.getAllDownloadedQuestionnaireIds().let {

        }
    }

    private fun uploadEmptyQuestionnaire(questionnaireId: String) = applicationScope.launch(Dispatchers.IO) {
        val response = try {
            backendRepository.insertEmptyFilledQuestionnaire(
                MongoFilledQuestionnaire(
                    questionnaireId = questionnaireId,
                    userId = preferencesRepository.userCredentialsFlow.first().id
                )
            )
        } catch (e: Exception) {
            localRepository.insert(LocallyDownloadedQuestionnaire(questionnaireId))
            null
        }

        when (response?.responseType) {
            InsertFilledQuestionnaireResponseType.INSERT_SUCCESSFUL -> {
                //EMPTY QUESTIONNAIRE INSERTED!
            }
            InsertFilledQuestionnaireResponseType.ERROR -> {
                //SOMETHING WENT WRONG
            }
            InsertFilledQuestionnaireResponseType.EMPTY_FILLED_QUESTIONNAIRE_NOT_INSERTED -> {
                //ES GIBT SCHON EINEN AUSGEFÜLLTEN QUESTIONNAIRE!
            }
            InsertFilledQuestionnaireResponseType.QUESTIONNAIRE_DOES_NOT_EXIST_ANYMORE -> {
                //QUESTIONNAIRE WURDE MITTLERWEILE GELÖSCHT!
            }
            null -> log("Empty one inserted!")
        }
    }

    sealed class FragmentHomeEvent {
        class ShowSnackBarMessageBar(val messageRes: Int) : FragmentHomeEvent()
    }
}