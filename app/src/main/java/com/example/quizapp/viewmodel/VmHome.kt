package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.QuizApplication
import com.example.quizapp.R
import com.example.quizapp.extensions.combine
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.isConnectedToInternet
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.QuestionnaireVisibility
import com.example.quizapp.model.databases.QuestionnaireVisibility.PRIVATE
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.databases.room.entities.LocallyFilledQuestionnaireToUpload
import com.example.quizapp.model.databases.room.entities.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.backendsyncer.BackendSyncer
import com.example.quizapp.model.ktor.responses.ChangeQuestionnaireVisibilityResponse.ChangeQuestionnaireVisibilityResponseType
import com.example.quizapp.model.ktor.responses.DeleteFilledQuestionnaireResponse.DeleteFilledQuestionnaireResponseType
import com.example.quizapp.model.ktor.responses.DeleteQuestionnaireResponse.DeleteQuestionnaireResponseType
import com.example.quizapp.model.ktor.responses.InsertFilledQuestionnaireResponse.InsertFilledQuestionnaireResponseType
import com.example.quizapp.model.ktor.responses.InsertQuestionnairesResponse.InsertQuestionnairesResponseType
import com.example.quizapp.model.ktor.status.SyncStatus.*
import com.example.quizapp.view.NavigationDispatcher
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.viewmodel.VmHome.*
import com.example.quizapp.viewmodel.VmHome.FragmentHomeEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
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
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle,
    app: QuizApplication
) : BaseViewModel<FragmentHomeEvent>() {

    init {
        if (app.isConnectedToInternet) {
            launch(IO, applicationScope) {
                eventChannel.send(ChangeProgressVisibility(true))
                backendSyncer.syncData()
                delay(500)
                eventChannel.send(ChangeProgressVisibility(false))
            }
        }
    }

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    val locallyPresentAuthors = localRepository.getAllLocalAuthorsFlow()
        .distinctUntilChanged()

    fun onLocallyPresentAuthorsChanged(locallyPresentAuthors: List<AuthorInfo>) = launch(IO) {
        preferencesRepository.getLocalFilteredAuthorIds().let { savedIds ->
            savedIds.filter { locallyPresentAuthors.none { author -> author.userId == it } }.let { notAvailableAuthorIds ->
                if (notAvailableAuthorIds.isNotEmpty()) {
                    preferencesRepository.updateLocalFilteredAuthorIds(savedIds - notAvailableAuthorIds.toSet())
                }
            }
        }
    }

    val completeQuestionnaireFlow = combine(
        searchQueryMutableStateFlow,
        preferencesRepository.localOrderByFlow,
        preferencesRepository.localAscendingOrderFlow,
        preferencesRepository.localFilteredAuthorIdsFlow,
        preferencesRepository.localFilteredCosIdsFlow,
        preferencesRepository.localFilteredFacultyIdsFlow,
        preferencesRepository.localFilterHideCompletedFlow
    ) { query, orderBy, ascending, authorIds, cosIds, facultyIds, hideCompleted ->
        localRepository.getFilteredCompleteQuestionnaireFlow(
            searchQuery = query,
            authorIds = authorIds,
            cosIds = cosIds,
            facultyIds = facultyIds,
            orderBy = orderBy,
            ascending = ascending,
            hideCompleted = hideCompleted
        )
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onSwipeRefreshTriggered() = launch(IO) {
        backendSyncer.synAllQuestionnaireData()
        eventChannel.send(ChangeProgressVisibility(false))
    }

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onClearSearchQueryClicked() = launch(IO) {
        if (searchQuery.isNotBlank()) {
            eventChannel.send(ClearSearchQueryEvent)
        }
    }

    fun onFilterButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToLocalQuestionnaireFilterDialog)
    }


    fun onCreatedItemSyncButtonClicked(questionnaireId: String) {
        applicationScope.launch(IO) {
            val completeQuestionnaire = localRepository.findCompleteQuestionnaireWith(questionnaireId)!!
            localRepository.update(completeQuestionnaire.questionnaire.copy(syncStatus = SYNCING))

            val result = try {
                backendRepository.insertQuestionnaire(completeQuestionnaire)
            } catch (e: Exception) {
                null
            }

            if (result != null && result.responseType == InsertQuestionnairesResponseType.SUCCESSFUL) {
                localRepository.update(completeQuestionnaire.questionnaire.copy(syncStatus = SYNCED))
                eventChannel.send(ShowSnackBarMessageBar(R.string.syncSuccessful))
            } else {
                localRepository.update(completeQuestionnaire.questionnaire.copy(syncStatus = UNSYNCED))
                eventChannel.send(ShowSnackBarMessageBar(R.string.syncUnsuccessful))
            }
        }
    }


    // DELETE CREATED QUESTIONNAIRE
    fun deleteCreatedQuestionnaire(questionnaireId: String) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let {
            eventChannel.send(ShowUndoDeleteCreatedQuestionnaireSnackBar(it))
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
            eventChannel.send(ShowUndoDeleteCachedQuestionnaireSnackBar(it))
        }
        localRepository.insert(LocallyDeletedQuestionnaire.notAsOwner(questionnaireId))
        localRepository.deleteQuestionnaireWith(questionnaireId)
    }

    fun onDeleteCachedQuestionnaireConfirmed(event: ShowUndoDeleteCachedQuestionnaireSnackBar) = launch(IO, applicationScope) {
        val questionnaireId = event.completeQuestionnaire.questionnaire.id

        runCatching {
            backendRepository.deleteFilledQuestionnaire(listOf(questionnaireId))
        }.onSuccess {
            if (it.responseType == DeleteFilledQuestionnaireResponseType.SUCCESSFUL) {
                localRepository.delete(LocallyDeletedQuestionnaire.notAsOwner(questionnaireId))
            }
        }
    }

    fun onUndoDeleteCachedQuestionnaireClicked(event: ShowUndoDeleteCachedQuestionnaireSnackBar) = launch(IO, applicationScope) {
        localRepository.insertCompleteQuestionnaire(event.completeQuestionnaire)
        localRepository.delete(LocallyDeletedQuestionnaire.notAsOwner(event.completeQuestionnaire.questionnaire.id))
    }


    // DELETE ANSWERS OF QUESTIONNAIRE
    fun deleteFilledQuestionnaire(questionnaireId: String) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let { completeQuestionnaire ->
            localRepository.insert(LocallyFilledQuestionnaireToUpload(completeQuestionnaire.questionnaire.id))
            eventChannel.send(ShowUndoDeleteAnswersOfQuestionnaireSnackBar(completeQuestionnaire))
            completeQuestionnaire.allAnswers.map { it.copy(isAnswerSelected = false) }.let {
                localRepository.update(it)
            }
        }
    }

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
                eventChannel.send(ShowSnackBarMessageBar(messageResource))
            } else {
                eventChannel.send(ShowSnackBarMessageBar(R.string.errorCouldNotChangeVisibility))
            }
        }.onFailure {
            eventChannel.send(ShowSnackBarMessageBar(R.string.errorCouldNotChangeVisibility))
        }
    }

    fun onQuestionnaireClicked(questionnaireId: String) = launch(IO) {
        navigationDispatcher.dispatch(ToQuizScreen(questionnaireId))
    }

    fun onQuestionnaireLongClicked(questionnaire: Questionnaire) = launch(IO) {
        navigationDispatcher.dispatch(ToQuestionnaireMoreOptionsDialog(questionnaire))
    }

    fun onAddQuestionnaireButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromHomeToAddEditQuestionnaire())
    }

    fun onSettingsButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromHomeToSettingsScreen)
    }

    fun onRemoteSearchButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromHomeToSearchScreen)
    }


    sealed class FragmentHomeEvent: ViewModelEventMarker {
        class ShowSnackBarMessageBar(val messageRes: Int) : FragmentHomeEvent()
        class ShowUndoDeleteCreatedQuestionnaireSnackBar(val completeQuestionnaire: CompleteQuestionnaire) : FragmentHomeEvent()
        class ShowUndoDeleteCachedQuestionnaireSnackBar(val completeQuestionnaire: CompleteQuestionnaire) : FragmentHomeEvent()
        class ShowUndoDeleteAnswersOfQuestionnaireSnackBar(val completeQuestionnaire: CompleteQuestionnaire) : FragmentHomeEvent()
        class ChangeProgressVisibility(val visible: Boolean) : FragmentHomeEvent()
        object ClearSearchQueryEvent : FragmentHomeEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
    }
}