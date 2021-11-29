package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
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
import com.example.quizapp.model.datastore.datawrappers.BrowsableOrderBy
import com.example.quizapp.model.ktor.responses.GetQuestionnaireResponse.*
import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.ktor.status.DownloadStatus.*
import com.example.quizapp.view.fragments.searchscreen.filterselection.BrowseQuestionnaireFilterSelectionResult
import com.example.quizapp.viewmodel.VmSearch.SearchEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import  com.example.quizapp.extensions.combine

@HiltViewModel
class VmSearch @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val searchEventChannel = Channel<SearchEvent>()

    val searchEventChannelFlow = searchEventChannel.receiveAsFlow()

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value

    private val selectedAuthorsMutableStateFlow = state.getMutableStateFlow(SELECTED_AUTHORS_KEY, emptySet<AuthorInfo>())

    private val selectedFacultyIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_FACULTY_ID_KEY, emptySet<String>())

    private val selectedCourseOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_COURSE_OF_STUDIES_ID_KEY, runBlocking(IO) {
        if (preferencesRepository.usePreferredCourseOfStudiesForSearch()) {
            preferencesRepository.getPreferredCourseOfStudiesId()
        } else {
            emptySet()
        }
    })

    val filteredPagedData = combine(
        preferencesRepository.browsableOrderByFlow,
        preferencesRepository.browsableAscendingOrderFlow,
        searchQueryMutableStateFlow,
        selectedAuthorsMutableStateFlow,
        selectedCourseOfStudiesIdsMutableStateFlow,
        selectedFacultyIdsMutableStateFlow,
    ) { browsableOrderBy: BrowsableOrderBy,
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
                browsableOrderBy = browsableOrderBy,
                ascending = ascending
            )
        }
    }.flatMapLatest(Pager<Int, BrowsableQuestionnaire>::flow::get).cachedIn(viewModelScope)


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
                NavigateToQuestionnaireFilterSelection(
                    selectedCourseOfStudiesIdsMutableStateFlow.value.toTypedArray(),
                    selectedFacultyIdsMutableStateFlow.value.toTypedArray(),
                    selectedAuthorsMutableStateFlow.value.toTypedArray()
                )
            )
        }
    }

    fun onQuestionnaireFilterUpdateReceived(newFilterBrowse: BrowseQuestionnaireFilterSelectionResult) {
        launch(IO) {
            newFilterBrowse.selectedAuthors.toSet().let {
                state.set(SELECTED_AUTHORS_KEY, it)
                selectedAuthorsMutableStateFlow.value = it
            }

            newFilterBrowse.selectedFacultyIds.toSet().let {
                state.set(SELECTED_FACULTY_ID_KEY, it)
                selectedFacultyIdsMutableStateFlow.value = it
            }

            newFilterBrowse.selectedCosIds.toSet().let {
                state.set(SELECTED_COURSE_OF_STUDIES_ID_KEY, it)
                selectedCourseOfStudiesIdsMutableStateFlow.value = it
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


    sealed class SearchEvent {
        class NavigateToQuestionnaireFilterSelection(
            val selectedCosIds: Array<String>,
            val selectedFaculties: Array<String>,
            val selectedAuthors: Array<AuthorInfo>
        ) : SearchEvent()
        object ClearSearchQueryEvent : SearchEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : SearchEvent()
        class ChangeItemDownloadStatusEvent(val questionnaireId: String, val status: DownloadStatus) : SearchEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_AUTHORS_KEY = "selectedUsersKeys"
        private const val SELECTED_COURSE_OF_STUDIES_ID_KEY = "selectedCourseOfStudiesKey"
        private const val SELECTED_FACULTY_ID_KEY = "selectedFacultyIdsKey"
    }
}