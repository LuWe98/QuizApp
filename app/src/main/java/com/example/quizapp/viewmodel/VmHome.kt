package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.example.quizapp.R
import com.example.quizapp.extensions.first
import com.example.quizapp.extensions.log
import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.ktor.paging.MongoQuestionnairePagingSource
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.ktor.responses.BackendResponse.InsertFilledQuestionnaireResponse.InsertFilledQuestionnaireResponseType
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.entities.LocallyDownloadedQuestionnaire
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.utils.SyncHelper
import com.example.quizapp.viewmodel.VmHome.FragmentHomeEvent.ShowSnackBarMessageBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VmHome @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    syncHelper: SyncHelper
) : ViewModel() {

    init { syncHelper.syncData() }

    private val userId = preferencesRepository.userCredentialsFlow.first(viewModelScope).id

    private val fragmentHomeEventChannel = Channel<FragmentHomeEvent>()

    val fragmentHomeEventChannelFlow get() = fragmentHomeEventChannel.receiveAsFlow()

    val allQuestionnairesWithQuestionsLD = localRepository.findAllQuestionnairesWithQuestionsNotForUserFlow(userId).asLiveData().distinctUntilChanged()

    val allQuestionnairesWithQuestionsForUserLD = localRepository.findAllQuestionnairesWithQuestionsForUserFlow(userId).asLiveData().distinctUntilChanged()



    val searchQuery = MutableLiveData("")

    val filteredPagedData = searchQuery.switchMap { query ->
        Pager(config = PagingConfig(pageSize = PagingConfigValues.PAGE_SIZE, maxSize = PagingConfigValues.MAX_SIZE),
            pagingSourceFactory = { MongoQuestionnairePagingSource(backendRepository, query) }).liveData
    }.cachedIn(viewModelScope).distinctUntilChanged()



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


    fun onCachedItemDownLoadButtonClicked(mongoQuestionnaire: MongoQuestionnaire) {
        applicationScope.launch(Dispatchers.IO) {
            DataMapper.mapMongoObjectToSqlEntities(mongoQuestionnaire).apply {
                localRepository.insert(questionnaire)
                localRepository.insert(allQuestions)
                localRepository.insert(allAnswers)
                uploadEmptyFilledQuestionnaire(questionnaire.id)
            }
        }
    }


    private fun uploadEmptyFilledQuestionnaire(questionnaireId: String) = applicationScope.launch(Dispatchers.IO) {
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
            InsertFilledQuestionnaireResponseType.INSERTED -> {
                //EMPTY QUESTIONNAIRE INSERTED!
            }
            InsertFilledQuestionnaireResponseType.ERROR -> {
                //SOMETHING WENT WRONG
            }
            InsertFilledQuestionnaireResponseType.EMPTY_INSERTION_SKIPPED -> {
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