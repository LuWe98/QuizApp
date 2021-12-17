package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quizapp.R
import com.example.quizapp.extensions.combine
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.datawrappers.RemoteQuestionnaireOrderBy
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.ktor.responses.GetQuestionnaireResponse.*
import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.ktor.status.DownloadStatus.*
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers.BrowseQuestionnaireMoreOptionsItem
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.*
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmSearch.*
import com.example.quizapp.viewmodel.VmSearch.SearchEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmSearch @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : BaseViewModel<SearchEvent>() {

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value

    private val selectedAuthorsMutableStateFlow = state.getMutableStateFlow(SELECTED_AUTHORS_KEY, emptySet<AuthorInfo>())


    val filteredPagedData = combine(
        preferencesRepository.browsableOrderByFlow,
        preferencesRepository.browsableAscendingOrderFlow,
        searchQueryMutableStateFlow,
        selectedAuthorsMutableStateFlow,
        preferencesRepository.browsableCosIdsFlow,
        preferencesRepository.browsableFacultyIdsFlow,
    ) { remoteQuestionnaireOrderBy: RemoteQuestionnaireOrderBy,
        ascending: Boolean,
        searchQuery: String,
        authors: Set<AuthorInfo>,
        cosIds: Set<String>,
        facultyIds: Set<String> ->
        PagingConfigValues.getDefaultPager { page ->
            backendRepository.getPagedQuestionnaires(
                page = page,
                searchString = searchQuery,
                questionnaireIdsToIgnore = localRepository.getAllQuestionnaireIds(),
                facultyIds = facultyIds.toList(),
                courseOfStudiesIds = cosIds.toList(),
                authorIds = authors.map(AuthorInfo::userId),
                remoteQuestionnaireOrderBy = remoteQuestionnaireOrderBy,
                ascending = ascending
            )
        }
    }.flatMapLatest(Pager<Int, BrowsableQuestionnaire>::flow::get)
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())


    suspend fun getCourseOfStudiesNameWithIds(courseOfStudiesIds: List<String>) = localRepository.getCoursesOfStudiesNameWithIds(courseOfStudiesIds)

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onClearSearchQueryClicked() = launch(IO) {
        if (searchQuery.isNotBlank()) {
            eventChannel.send(ClearSearchQueryEvent)
        }
    }

    fun onBackButtonClicked() = launch(IO)  {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onFilterButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToRemoteQuestionnaireFilterDialog(selectedAuthorsMutableStateFlow.value.toTypedArray()))
    }

    fun onRemoteQuestionnaireFilterUpdateReceived(result: FragmentResult.RemoteQuestionnaireFilterResult){
        result.selectedAuthors.toSet().let {
            state.set(SELECTED_AUTHORS_KEY, it)
            selectedAuthorsMutableStateFlow.value = it
        }
    }


    //TODO -> Ist nur temporär um zu testen wegen main screen
    fun onItemDownLoadButtonClicked(questionnaireId: String) = launch(IO) {
        eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DOWNLOADING))

        runCatching {
            backendRepository.downloadQuestionnaire(questionnaireId)
        }.onSuccess { response ->
            when (response.responseType) {
                GetQuestionnaireResponseType.SUCCESSFUL -> {
                    DataMapper.mapMongoQuestionnaireToRoomCompleteQuestionnaire(response.mongoQuestionnaire!!).let { completeQuestionnaire ->
                        localRepository.insertCompleteQuestionnaire(completeQuestionnaire)
                        localRepository.deleteLocallyDeletedQuestionnaireWith(completeQuestionnaire.questionnaire.id)

                        DataMapper.mapMongoQuestionnaireToRoomQuestionnaireCourseOfStudiesRelation(response.mongoQuestionnaire).let {
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

    fun onItemLongClicked(browsableQuestionnaire: BrowsableQuestionnaire) = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.BrowseQuestionnaireMoreOptionsSelection(browsableQuestionnaire)))
    }

    fun onItemClicked(browsableQuestionnaire: BrowsableQuestionnaire) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(browsableQuestionnaire.id)?.let {
            navigationDispatcher.dispatch(ToQuizScreen(it.questionnaire.id))
            return@launch
        }
        downLoadQuestionnaire(browsableQuestionnaire.id)
    }

    fun onQuestionnaireMoreOptionsSelectionResultReceived(result: SelectionResult.RemoteQuestionnaireMoreOptionsSelectionResult) {
        when (result.selectedItem) {
            BrowseQuestionnaireMoreOptionsItem.DOWNLOAD -> onItemDownLoadButtonClicked(result.calledOnRemoteQuestionnaire.id)
            BrowseQuestionnaireMoreOptionsItem.OPEN -> onItemClicked(result.calledOnRemoteQuestionnaire)
        }
    }


    private suspend fun downLoadQuestionnaire(questionnaireId: String) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.downloadingQuestionnaire))

        runCatching {
            backendRepository.downloadQuestionnaire(questionnaireId)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
        }.onSuccess { response ->
            when (response.responseType) {
                GetQuestionnaireResponseType.SUCCESSFUL -> {
                    DataMapper.mapMongoQuestionnaireToRoomCompleteQuestionnaire(response.mongoQuestionnaire!!).let { completeQuestionnaire ->
                        localRepository.insertCompleteQuestionnaire(completeQuestionnaire)
                        localRepository.deleteLocallyDeletedQuestionnaireWith(completeQuestionnaire.questionnaire.id)

                        DataMapper.mapMongoQuestionnaireToRoomQuestionnaireCourseOfStudiesRelation(response.mongoQuestionnaire).let {
                            localRepository.insert(it)
                        }

                        eventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DOWNLOADED))
                        navigationDispatcher.dispatch(ToQuizScreen(questionnaireId))
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


    sealed class SearchEvent: ViewModelEventMarker {
        object ClearSearchQueryEvent : SearchEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : SearchEvent()
        class ChangeItemDownloadStatusEvent(val questionnaireId: String, val status: DownloadStatus) : SearchEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_AUTHORS_KEY = "selectedUsersKeys"
    }
}