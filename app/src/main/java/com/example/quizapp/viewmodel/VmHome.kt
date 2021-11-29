package com.example.quizapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.app
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.isConnectedToInternet
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.QuestionnaireVisibility
import com.example.quizapp.model.databases.QuestionnaireVisibility.PRIVATE
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.databases.room.entities.sync.LocallyFilledQuestionnaireToUpload
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.ChangeQuestionnaireVisibilityResponse.ChangeQuestionnaireVisibilityResponseType
import com.example.quizapp.model.ktor.responses.DeleteFilledQuestionnaireResponse.DeleteFilledQuestionnaireResponseType
import com.example.quizapp.model.ktor.responses.DeleteQuestionnaireResponse.DeleteQuestionnaireResponseType
import com.example.quizapp.model.ktor.responses.InsertFilledQuestionnaireResponse.InsertFilledQuestionnaireResponseType
import com.example.quizapp.model.ktor.responses.InsertQuestionnairesResponse.InsertQuestionnairesResponseType
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.ktor.backendsyncer.BackendSyncer
import com.example.quizapp.viewmodel.VmHome.FragmentHomeEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VmHome @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val backendSyncer: BackendSyncer,
    preferencesRepository: PreferencesRepository,
    application: Application,
    private val state: SavedStateHandle
) : AndroidViewModel(application) {

    private val fragmentHomeEventChannel = Channel<FragmentHomeEvent>()

    val fragmentHomeEventChannelFlow = fragmentHomeEventChannel.receiveAsFlow()

    init {
        if (app.isConnectedToInternet) {
            applicationScope.launch(IO) {
                fragmentHomeEventChannel.send(ChangeProgressVisibility(true))
                backendSyncer.syncData()
                delay(500)
                fragmentHomeEventChannel.send(ChangeProgressVisibility(false))
            }
        }
    }

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


//    private val userIdFlow = preferencesRepository.userIdFlow.flowOn(IO)
//
//    val allCachedQuestionnairesFlow = userIdFlow
//        .flatMapLatest(localRepository::findAllCompleteQuestionnairesNotForUserFlow)
//        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
//
//    val allCreatedQuestionnairesFlow = userIdFlow
//        .flatMapLatest(localRepository::findAllCompleteQuestionnairesForUserFlow)
//        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    val allCompleteQuestionnaireFlow = searchQueryMutableStateFlow.flatMapLatest { query ->
        localRepository.getFilteredCompleteQuestionnaireFlow(query)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onSwipeRefreshTriggered() = launch(IO) {
        backendSyncer.synAllQuestionnaireData()
        fragmentHomeEventChannel.send(ChangeProgressVisibility(false))
    }

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onClearSearchQueryClicked(){
        if(searchQuery.isNotBlank()) {
            launch {
                fragmentHomeEventChannel.send(ClearSearchQueryEvent)
            }
        }
    }

    fun onFilterButtonClicked(){
        launch(IO) {
            fragmentHomeEventChannel.send(NavigateToLocalQuestionnairesFilterSelection())
        }
    }


    fun onCreatedItemSyncButtonClicked(questionnaireId: String) {
        applicationScope.launch(IO) {
            val completeQuestionnaire = localRepository.findCompleteQuestionnaireWith(questionnaireId)!!
            localRepository.update(completeQuestionnaire.questionnaire.apply { syncStatus = SyncStatus.SYNCING })

            val result = try {
                backendRepository.insertQuestionnaire(completeQuestionnaire)
            } catch (e: Exception) {
                null
            }

            if (result != null && result.responseType == InsertQuestionnairesResponseType.SUCCESSFUL) {
                localRepository.update(completeQuestionnaire.questionnaire.apply { syncStatus = SyncStatus.SYNCED })
                fragmentHomeEventChannel.send(ShowSnackBarMessageBar(R.string.syncSuccessful))
            } else {
                localRepository.update(completeQuestionnaire.questionnaire.apply { syncStatus = SyncStatus.UNSYNCED })
                fragmentHomeEventChannel.send(ShowSnackBarMessageBar(R.string.syncUnsuccessful))
            }
        }
    }


    // DELETE CREATED QUESTIONNAIRE
    fun deleteCreatedQuestionnaire(questionnaireId: String) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let {
            fragmentHomeEventChannel.send(ShowUndoDeleteCreatedQuestionnaireSnackBar(it))
        }
        localRepository.insert(LocallyDeletedQuestionnaire.asOwner(questionnaireId))
        localRepository.deleteQuestionnaireWith(questionnaireId)
    }

    fun onDeleteCreatedQuestionnaireConfirmed(event: ShowUndoDeleteCreatedQuestionnaireSnackBar) = launch(IO) {
        val questionnaireId = event.completeQuestionnaire.questionnaire.id

        runCatching {
            backendRepository.deleteQuestionnaire(listOf(questionnaireId))
        }.onSuccess {
            if (it.responseType == DeleteQuestionnaireResponseType.SUCCESSFUL) {
                localRepository.delete(LocallyDeletedQuestionnaire.asOwner(questionnaireId))
            }
        }
    }

    fun onUndoDeleteCreatedQuestionnaireClicked(event: ShowUndoDeleteCreatedQuestionnaireSnackBar) = launch(IO) {
        localRepository.insertCompleteQuestionnaire(event.completeQuestionnaire)
        localRepository.delete(LocallyDeletedQuestionnaire.asOwner(event.completeQuestionnaire.questionnaire.id))
    }


    // DELETE FILLED QUESTIONNAIRE
    fun deleteCachedQuestionnaire(questionnaireId: String) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let {
            fragmentHomeEventChannel.send(ShowUndoDeleteCachedQuestionnaireSnackBar(it))
        }
        localRepository.insert(LocallyDeletedQuestionnaire.notAsOwner(questionnaireId))
        localRepository.deleteQuestionnaireWith(questionnaireId)
    }

    fun onDeleteCachedQuestionnaireConfirmed(event: ShowUndoDeleteCachedQuestionnaireSnackBar) = applicationScope.launch(IO) {
        val questionnaireId = event.completeQuestionnaire.questionnaire.id

        runCatching {
            backendRepository.deleteFilledQuestionnaire(listOf(questionnaireId))
        }.onSuccess {
            if (it.responseType == DeleteFilledQuestionnaireResponseType.SUCCESSFUL) {
                localRepository.delete(LocallyDeletedQuestionnaire.notAsOwner(questionnaireId))
            }
        }
    }

    fun onUndoDeleteCachedQuestionnaireClicked(event: ShowUndoDeleteCachedQuestionnaireSnackBar) = applicationScope.launch(IO) {
        localRepository.insertCompleteQuestionnaire(event.completeQuestionnaire)
        localRepository.delete(LocallyDeletedQuestionnaire.notAsOwner(event.completeQuestionnaire.questionnaire.id))
    }


    // DELETE ANSWERS OF QUESTIONNAIRE
    fun deleteFilledQuestionnaire(questionnaireId: String) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let { completeQuestionnaire ->
            localRepository.insert(LocallyFilledQuestionnaireToUpload(completeQuestionnaire.questionnaire.id))
            fragmentHomeEventChannel.send(ShowUndoDeleteAnswersOfQuestionnaireSnackBar(completeQuestionnaire))
            completeQuestionnaire.allAnswers.map { it.copy(isAnswerSelected = false) }.let {
                localRepository.update(it)
            }
        }
    }

    //TODO -> Das ist der einzige kritische Punkt, da hier nur die antworten gelöscht werden, deswegen wird später der Fragebogen nicht automatisch rausgefiltert
    fun onDeleteFilledQuestionnaireConfirmed(event: ShowUndoDeleteAnswersOfQuestionnaireSnackBar) = launch(IO) {
        runCatching {
            backendRepository.insertFilledQuestionnaire(DataMapper.mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(event.completeQuestionnaire))
        }.onSuccess { response ->
            if (response.responseType != InsertFilledQuestionnaireResponseType.NOT_ACKNOWLEDGED) {
                localRepository.delete(LocallyFilledQuestionnaireToUpload(event.completeQuestionnaire.questionnaire.id))
            }
        }
    }

    fun onUndoDeleteFilledQuestionnaireClicked(event: ShowUndoDeleteAnswersOfQuestionnaireSnackBar) = launch(IO) {
        localRepository.update(event.completeQuestionnaire.allAnswers)
    }


    fun onChangeQuestionnaireVisibilitySelected(questionnaireId: String, newVisibility: QuestionnaireVisibility) = launch(IO) {
        runCatching {
            backendRepository.changeQuestionnaireVisibility(questionnaireId, newVisibility)
        }.onSuccess { response ->
            if (response.responseType == ChangeQuestionnaireVisibilityResponseType.SUCCESSFUL) {
                localRepository.findQuestionnaireWith(questionnaireId)?.let {
                    localRepository.update(it.copy(visibility = newVisibility))
                }

                val messageResource = if (newVisibility == PRIVATE) R.string.changedVisibilityToPrivate else R.string.changedVisibilityToPublic
                fragmentHomeEventChannel.send(ShowSnackBarMessageBar(messageResource))
            } else {
                fragmentHomeEventChannel.send(ShowSnackBarMessageBar(R.string.errorCouldNotChangeVisibility))
            }
        }.onFailure {
            fragmentHomeEventChannel.send(ShowSnackBarMessageBar(R.string.errorCouldNotChangeVisibility))
        }
    }


    sealed class FragmentHomeEvent {
        class ShowSnackBarMessageBar(val messageRes: Int) : FragmentHomeEvent()
        class ShowUndoDeleteCreatedQuestionnaireSnackBar(val completeQuestionnaire: CompleteQuestionnaire) : FragmentHomeEvent()
        class ShowUndoDeleteCachedQuestionnaireSnackBar(val completeQuestionnaire: CompleteQuestionnaire, ) : FragmentHomeEvent()
        class ShowUndoDeleteAnswersOfQuestionnaireSnackBar(val completeQuestionnaire: CompleteQuestionnaire) : FragmentHomeEvent()
        class ChangeProgressVisibility(val visible: Boolean) : FragmentHomeEvent()
        class NavigateToLocalQuestionnairesFilterSelection() : FragmentHomeEvent()
        object ClearSearchQueryEvent: FragmentHomeEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
    }
}