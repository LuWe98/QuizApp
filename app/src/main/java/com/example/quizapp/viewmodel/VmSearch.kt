package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.datastore.datawrappers.RemoteQuestionnaireOrderBy
import com.example.quizapp.model.ktor.responses.GetQuestionnaireResponse.*
import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.ktor.status.DownloadStatus.*
import com.example.quizapp.viewmodel.VmSearch.SearchEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import  com.example.quizapp.extensions.combine
import com.example.quizapp.model.selection.datawrappers.BrowseQuestionnaireMoreOptionsItem
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import kotlinx.coroutines.delay

@HiltViewModel
class VmSearch @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val searchEventChannel = Channel<SearchEvent>()

    val searchEventChannelFlow = searchEventChannel.receiveAsFlow()

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value

    private val selectedAuthorsMutableStateFlow = state.getMutableStateFlow(SELECTED_AUTHORS_KEY, emptySet<AuthorInfo>())

//    private val selectedFacultyIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_FACULTY_ID_KEY, emptySet<String>())
//
//    private val selectedCourseOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_COURSE_OF_STUDIES_ID_KEY, runBlocking(IO) {
//        if (preferencesRepository.usePreferredCourseOfStudiesForSearch()) {
//            preferencesRepository.getPreferredCourseOfStudiesId()
//        } else {
//            emptySet()
//        }
//    })

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

    fun onClearSearchQueryClicked() {
        if (searchQuery.isNotBlank()) {
            launch(IO) {
                searchEventChannel.send(ClearSearchQueryEvent)
            }
        }
    }

    fun onFilterButtonClicked() {
        launch(IO) {
            searchEventChannel.send(
                NavigateToQuestionnaireFilterSelection(selectedAuthorsMutableStateFlow.value.toTypedArray())
            )
        }
    }

    fun onQuestionnaireFilterUpdateReceived(selectedAuthors: Array<AuthorInfo>) {
        launch(IO) {
            selectedAuthors.toSet().let {
                state.set(SELECTED_AUTHORS_KEY, it)
                selectedAuthorsMutableStateFlow.value = it
            }
        }
    }


    //TODO -> Ist nur temporär um zu testen wegen main screen
    fun onItemDownLoadButtonClicked(questionnaireId: String) = launch(IO) {
        searchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DOWNLOADING))

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

                        searchEventChannel.send(ShowMessageSnackBar(R.string.questionnaireDownloaded))
                        searchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DOWNLOADED))
                    }
                }
                GetQuestionnaireResponseType.QUESTIONNAIRE_NOT_FOUND -> {
                    searchEventChannel.send(ShowMessageSnackBar(R.string.errorQuestionnaireCouldNotBeFound))
                    searchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, NOT_DOWNLOADED))
                }
            }
        }.onFailure {
            searchEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDownloadQuestionnaire))
            searchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, NOT_DOWNLOADED))
        }
    }

    fun onItemLongClicked(browsableQuestionnaire: BrowsableQuestionnaire) {
        launch(IO) {
            searchEventChannel.send(NavigateToSelectionScreen(SelectionType.BrowseQuestionnaireMoreOptionsSelection(browsableQuestionnaire)))
        }
    }

    fun onItemClicked(browsableQuestionnaire: BrowsableQuestionnaire) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(browsableQuestionnaire.id)?.let {
            searchEventChannel.send(NavigateToQuizScreen(it.questionnaire.id))
            return@launch
        }

        downLoadQuestionnaire(browsableQuestionnaire.id)
    }

    fun onMoreOptionsItemClickedUpdateReceived(
        clickedItem: BrowseQuestionnaireMoreOptionsItem,
        selectionType: SelectionType.BrowseQuestionnaireMoreOptionsSelection,
    ) {
        when (clickedItem) {
            BrowseQuestionnaireMoreOptionsItem.DOWNLOAD -> onItemDownLoadButtonClicked(selectionType.browsableQuestionnaire.id)
            BrowseQuestionnaireMoreOptionsItem.OPEN -> onItemClicked(selectionType.browsableQuestionnaire)
        }
    }



    private suspend fun downLoadQuestionnaire(questionnaireId: String) {
        searchEventChannel.send(ShowLoadingDialog(R.string.downloadingQuestionnaire))

        runCatching {
            backendRepository.downloadQuestionnaire(questionnaireId)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            searchEventChannel.send(HideLoadingDialog)
        }.onSuccess { response ->
            when (response.responseType) {
                GetQuestionnaireResponseType.SUCCESSFUL -> {
                    DataMapper.mapMongoQuestionnaireToRoomCompleteQuestionnaire(response.mongoQuestionnaire!!).let { completeQuestionnaire ->
                        localRepository.insertCompleteQuestionnaire(completeQuestionnaire)
                        localRepository.deleteLocallyDeletedQuestionnaireWith(completeQuestionnaire.questionnaire.id)

                        DataMapper.mapMongoQuestionnaireToRoomQuestionnaireCourseOfStudiesRelation(response.mongoQuestionnaire).let {
                            localRepository.insert(it)
                        }

                        searchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DOWNLOADED))
                        searchEventChannel.send(NavigateToQuizScreen(questionnaireId))
                    }
                }
                GetQuestionnaireResponseType.QUESTIONNAIRE_NOT_FOUND -> {
                    searchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, NOT_DOWNLOADED))
                }
            }
        }.onFailure {
            searchEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDownloadQuestionnaire))
            searchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, NOT_DOWNLOADED))
        }
    }


    sealed class SearchEvent {
        class NavigateToQuestionnaireFilterSelection(val selectedAuthors: Array<AuthorInfo>) : SearchEvent()
        object ClearSearchQueryEvent : SearchEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : SearchEvent()
        class ChangeItemDownloadStatusEvent(val questionnaireId: String, val status: DownloadStatus) : SearchEvent()
        class NavigateToSelectionScreen(val selectionType: SelectionType) : SearchEvent()
        class ShowLoadingDialog(@StringRes val messageRes: Int) : SearchEvent()
        object HideLoadingDialog : SearchEvent()
        class NavigateToQuizScreen(val questionnaireId: String): SearchEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_AUTHORS_KEY = "selectedUsersKeys"
    }
}