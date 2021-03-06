package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.quizapp.R
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.dto.MongoBrowsableQuestionnaire
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.datastore.PreferenceRepository
import com.example.quizapp.model.datastore.datawrappers.BrowsableQuestionnaireOrderBy
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse
import com.example.quizapp.model.ktor.BackendResponse.GetQuestionnaireResponse.*
import com.example.quizapp.model.ktor.paging.*
import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.ktor.status.DownloadStatus.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.datawrappers.BrowseQuestionnaireMoreOptionsItem
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmSearch.*
import com.example.quizapp.viewmodel.VmSearch.SearchEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmSearch @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val dataMapper: DataMapper,
    private val preferenceRepository: PreferenceRepository,
    private val state: SavedStateHandle
) : EventViewModel<SearchEvent>() {

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value

    private val selectedAuthorsMutableStateFlow = state.getMutableStateFlow(SELECTED_AUTHORS_KEY, emptySet<AuthorInfo>())

    private val selectedAuthors get() = selectedAuthorsMutableStateFlow.value

    private var previousPagerRefreshState: LoadState? = null


    val filteredPagedData = combine(
        preferenceRepository.browsableOrderByFlow,
        preferenceRepository.browsableAscendingOrderFlow,
        searchQueryMutableStateFlow,
        selectedAuthorsMutableStateFlow,
        preferenceRepository.browsableCosIdsFlow,
        preferenceRepository.browsableFacultyIdsFlow,
    ) { orderBy: BrowsableQuestionnaireOrderBy,
        ascending: Boolean,
        searchQuery: String,
        authors: Set<AuthorInfo>,
        cosIds: Set<String>,
        facultyIds: Set<String> ->
        Pager(
            config = PagingConfigUtil.defaultPagingConfig,
            pagingSourceFactory = {
                BrowseQuestionnairePagingSource(orderBy) {
                    backendRepository.questionnaireApi.getPagedQuestionnaires(
                        lastPageKeys = it,
                        limit = 30,
                        searchString = searchQuery,
                        questionnaireIdsToIgnore = localRepository.getAllQuestionnaireIds(),
                        facultyIds = facultyIds.toList(),
                        courseOfStudiesIds = cosIds.toList(),
                        authorIds = authors.map(AuthorInfo::userId),
                        orderBy = orderBy,
                        ascending = ascending
                    )
                }
            }
        )
    }.flatMapLatest(Pager<BrowsableQuestionnairePageKeys, MongoBrowsableQuestionnaire>::flow::get)
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())


    suspend fun getCourseOfStudiesNameWithIds(courseOfStudiesIds: List<String>) = localRepository.getCoursesOfStudiesNameWithIds(courseOfStudiesIds)

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onClearSearchQueryClicked() = launch(IO) {
        //navigationDispatcher.dispatch(ToVoiceSearchDialog)

        if (searchQuery.isNotEmpty()) {
            eventChannel.send(ClearSearchQueryEvent)
        }
    }

    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onFilterButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToRemoteQuestionnaireFilterDialog(selectedAuthorsMutableStateFlow.value))
    }

    fun onRemoteQuestionnaireFilterUpdateReceived(result: FragmentResult.RemoteQuestionnaireFilterResult) {
        result.selectedAuthors.toSet().let {
            state.set(SELECTED_AUTHORS_KEY, it)
            selectedAuthorsMutableStateFlow.value = it
        }
    }

    fun onQuestionnaireMoreOptionsSelectionResultReceived(result: SelectionResult.RemoteQuestionnaireMoreOptionsSelectionResult) {
        when (result.selectedItem) {
            BrowseQuestionnaireMoreOptionsItem.DOWNLOAD -> onItemDownLoadButtonClicked(result.calledOnRemoteQuestionnaire.id)
            BrowseQuestionnaireMoreOptionsItem.OPEN -> onItemClicked(result.calledOnRemoteQuestionnaire)
            BrowseQuestionnaireMoreOptionsItem.DELETE -> deleteDownloadedQuestionnaire(result.calledOnRemoteQuestionnaire.id)
        }
    }

    fun onItemDownLoadButtonClicked(questionnaireId: String) = launch(IO) {
        eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DOWNLOADING))

        runCatching {
            backendRepository.questionnaireApi.downloadQuestionnaire(questionnaireId)
        }.onSuccess { response ->
            when (response.responseType) {
                GetQuestionnaireResponseType.SUCCESSFUL -> {
                    dataMapper.mapMongoQuestionnaireToRoomCompleteQuestionnaire(response.mongoQuestionnaire!!).let { completeQuestionnaire ->
                        localRepository.insertCompleteQuestionnaire(completeQuestionnaire)
                        localRepository.deleteLocallyDeletedQuestionnaireWith(completeQuestionnaire.questionnaire.id)

                        dataMapper.mapMongoQuestionnaireToRoomQuestionnaireCourseOfStudiesRelation(response.mongoQuestionnaire).let {
                            localRepository.insert(it)
                        }

                        eventChannel.send(ShowMessageSnackBar(R.string.questionnaireDownloaded))
                        eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DOWNLOADED))
                    }
                }
                GetQuestionnaireResponseType.QUESTIONNAIRE_NOT_FOUND -> {
                    eventChannel.send(ShowMessageSnackBar(R.string.errorQuestionnaireCouldNotBeFound))
                    eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, NOT_DOWNLOADED))
                }
            }
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDownloadQuestionnaire))
            eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, NOT_DOWNLOADED))
        }
    }

    fun onItemLongClicked(browsableQuestionnaire: MongoBrowsableQuestionnaire) = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.BrowseQuestionnaireMoreOptionsSelection(browsableQuestionnaire)))
    }

    fun onItemClicked(browsableQuestionnaire: MongoBrowsableQuestionnaire) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(browsableQuestionnaire.id)?.let {
            navigationDispatcher.dispatch(ToQuizScreen(it))
            return@launch
        }
        downLoadQuestionnaire(browsableQuestionnaire.id)
    }

    private fun deleteDownloadedQuestionnaire(questionnaireId: String) = launch(IO) {
        localRepository.insert(LocallyDeletedQuestionnaire.notAsOwner(questionnaireId))
        localRepository.deleteQuestionnaireWith(questionnaireId)
        eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, NOT_DOWNLOADED))

        runCatching {
            backendRepository.filledQuestionnaireApi.deleteFilledQuestionnaire(listOf(questionnaireId))
        }.onSuccess {
            if (it.responseType == BackendResponse.DeleteFilledQuestionnaireResponse.DeleteFilledQuestionnaireResponseType.SUCCESSFUL) {
                localRepository.delete(LocallyDeletedQuestionnaire.notAsOwner(questionnaireId))
            }
        }
    }

    private suspend fun downLoadQuestionnaire(questionnaireId: String) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.downloadingQuestionnaire))

        runCatching {
            backendRepository.questionnaireApi.downloadQuestionnaire(questionnaireId)
        }.also {
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
        }.onSuccess { response ->
            when (response.responseType) {
                GetQuestionnaireResponseType.SUCCESSFUL -> {
                    dataMapper.mapMongoQuestionnaireToRoomCompleteQuestionnaire(response.mongoQuestionnaire!!).let { completeQuestionnaire ->
                        localRepository.insertCompleteQuestionnaire(completeQuestionnaire)
                        localRepository.deleteLocallyDeletedQuestionnaireWith(completeQuestionnaire.questionnaire.id)

                        dataMapper.mapMongoQuestionnaireToRoomQuestionnaireCourseOfStudiesRelation(response.mongoQuestionnaire).let {
                            localRepository.insert(it)
                        }

                        eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DOWNLOADED))

                        navigationDispatcher.dispatch(ToQuizScreen(completeQuestionnaire))
                    }
                }
                GetQuestionnaireResponseType.QUESTIONNAIRE_NOT_FOUND -> {
                    eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, NOT_DOWNLOADED))
                }
            }
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDownloadQuestionnaire))
            eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, NOT_DOWNLOADED))
        }
    }

    fun onLoadStateChanged(loadStates: CombinedLoadStates, itemCount: Int) = launch(IO) {
        PagingUiState.fromCombinedLoadStates(
            loadStates = loadStates,
            previousLoadState = previousPagerRefreshState,
            itemCount = itemCount
        ) {
            searchQuery.isNotEmpty()
                    || selectedAuthors.isNotEmpty()
                    || preferenceRepository.getBrowsableFilteredCosIds().isNotEmpty()
                    || preferenceRepository.getBrowsableFilteredFacultyIds().isNotEmpty()
        }.also { state ->
            state?.let(::NewPagingUiStateEvent)?.let {
                eventChannel.send(it)
            }
        }
        previousPagerRefreshState = loadStates.source.refresh
    }

    sealed class SearchEvent : UiEventMarker {
        object ClearSearchQueryEvent : SearchEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : SearchEvent()
        class ChangeItemDownloadStatusEvent(val questionnaireId: String, val status: DownloadStatus) : SearchEvent()
        class NewPagingUiStateEvent(val state: PagingUiState) : SearchEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_AUTHORS_KEY = "selectedUsersKeys"
    }
}