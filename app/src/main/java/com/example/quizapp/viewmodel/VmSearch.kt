package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.MongoQuestionnairePagingSource
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.menus.SortBy
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

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

    val searchQuery get() = searchQueryMutableStateFlow.value


    private val selectedUserCreatorsMutableStateFlow = state.getMutableStateFlow(SELECTED_USERS_KEY, emptySet<User>())

    val selectedUserCreatorsStateFlow = selectedUserCreatorsMutableStateFlow.asStateFlow()

    private val selectedUserCreators get() = selectedUserCreatorsMutableStateFlow.value


    private val selectedCourseOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_COURSE_OF_STUDIES_ID_KEY, runBlocking(IO) {
        if (preferencesRepository.usePreferredCourseOfStudiesForSearch()) {
            preferencesRepository.getPreferredCourseOfStudiesId()
        } else {
            emptySet()
        }
    })

    private val selectedCourseOfStudiesIds get() = selectedCourseOfStudiesIdsMutableStateFlow.value

    val selectedCourseOfStudiesStateFlow = selectedCourseOfStudiesIdsMutableStateFlow.map {
        localRepository.getCoursesOfStudiesWithIds(it)
    }.distinctUntilChanged()


    private val selectedFacultyIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_FACULTY_ID_KEY, emptySet<String>())

    private val selectedFacultyIds get() = selectedFacultyIdsMutableStateFlow.value

    val selectedFacultyStateFlow = selectedFacultyIdsMutableStateFlow.map {
        localRepository.getFacultiesWithIds(it.toList())
    }.distinctUntilChanged()


    val sortByFlow = preferencesRepository.sortByFlow

    val filteredPagedData = combine(
        sortByFlow,
        searchQueryMutableStateFlow,
        selectedUserCreatorsMutableStateFlow,
        selectedCourseOfStudiesIdsMutableStateFlow,
        selectedFacultyIdsMutableStateFlow
    ) { sortBy: SortBy, searchQuery: String, users: Set<User>, cosIds: Set<String>, facultyIds: Set<String> ->
        Pager(
            config = PagingConfig(pageSize = PagingConfigValues.PAGE_SIZE, maxSize = PagingConfigValues.MAX_SIZE),
            pagingSourceFactory = {
                MongoQuestionnairePagingSource(
                    backendRepository,
                    localRepository,
                    searchQuery,
                    facultyIds.toList(),
                    cosIds.toList(),
                    users.map(User::id),
                    sortBy
                )
            })
    }.flatMapLatest {
        it.flow.cachedIn(viewModelScope)
    }.stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())


    suspend fun getCourseOfStudiesNameWithIds(courseOfStudiesIds: List<String>) = localRepository.getCoursesOfStudiesNameWithIds(courseOfStudiesIds)


//    val filteredPagedData = searchQueryMutableStateFlow.flatMapLatest { query ->
//        Pager(
//            config = PagingConfig(pageSize = PagingConfigValues.PAGE_SIZE, maxSize = PagingConfigValues.MAX_SIZE),
//            pagingSourceFactory = {
//                MongoQuestionnairePagingSource(backendRepository, localRepository, query)
//            }).flow.cachedIn(viewModelScope)
//    }


    fun removeFilteredFaculty(faculty: Faculty) {
        selectedFacultyIds.toMutableSet().apply {
            remove(faculty.id)
            state.set(SELECTED_FACULTY_ID_KEY, this)
            selectedFacultyIdsMutableStateFlow.value = this
        }
    }

    fun removeFilteredCourseOfStudies(courseOfStudies: CourseOfStudies) {
        selectedCourseOfStudiesIds.toMutableSet().apply {
            remove(courseOfStudies.id)
            state.set(SELECTED_COURSE_OF_STUDIES_ID_KEY, this)
            selectedCourseOfStudiesIdsMutableStateFlow.value = this
        }
    }

    fun removeFilteredUser(user: User) {
        selectedUserCreators.toMutableSet().apply {
            remove(user)
            state.set(SELECTED_USERS_KEY, this)
            selectedUserCreatorsMutableStateFlow.value = this
        }
    }


    fun onSortByCardClicked() {
        launch(IO) {
            searchEventChannel.send(SearchEvent.NavigateToSortBySelection(sortByFlow.first()))
        }
    }


    fun onFacultyCardAddButtonClicked() {
        launch(IO) {
            searchEventChannel.send(SearchEvent.NavigateToFacultySelectionScreen(selectedFacultyIds.toTypedArray()))
        }
    }

    fun onCourseOfStudiesAddButtonClicked() {
        launch(IO) {
            searchEventChannel.send(SearchEvent.NavigateToCourseOfStudiesSelectionScreen(selectedCourseOfStudiesIds.toTypedArray()))
        }
    }

    fun onAuthorAddButtonClicked() {
        launch(IO) {
            searchEventChannel.send(SearchEvent.NavigateToUserSelectionScreen(selectedUserCreators.toTypedArray()))
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onSortByUpdateReceived(sortBy: SortBy) {
        launch(IO) {
            preferencesRepository.updateSortBy(sortBy)
        }
    }

    fun onSelectedUserUpdateReceived(selectedUsers: Array<User>) {
        selectedUsers.toSet().let {
            state.set(SELECTED_USERS_KEY, it)
            selectedUserCreatorsMutableStateFlow.value = it
        }
    }

    fun onSelectedFacultyUpdateReceived(selectedFacultyIds: Array<String>) {
        selectedFacultyIds.toSet().let {
            state.set(SELECTED_FACULTY_ID_KEY, it)
            selectedFacultyIdsMutableStateFlow.value = it
        }
    }

    fun onSelectedCourseOfStudiesUpdateReceived(selectedCourseOfStudiesIds: Array<String>) {
        selectedCourseOfStudiesIds.toSet().let {
            state.set(SELECTED_COURSE_OF_STUDIES_ID_KEY, it)
            selectedCourseOfStudiesIdsMutableStateFlow.value = it
        }
    }


    sealed class SearchEvent {
        class NavigateToSortBySelection(val sortBy: SortBy) : SearchEvent()
        class NavigateToUserSelectionScreen(val selectedUsers: Array<User>) : SearchEvent()
        class NavigateToCourseOfStudiesSelectionScreen(val selectedCourseOfStudiesIds: Array<String>) : SearchEvent()
        class NavigateToFacultySelectionScreen(val selectedFacultyIds: Array<String>) : SearchEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_USERS_KEY = "selectedUsersKeys"
        private const val SELECTED_COURSE_OF_STUDIES_ID_KEY = "selectedCourseOfStudiesKey"
        private const val SELECTED_FACULTY_ID_KEY = "selectedFacultyIdsKey"
    }
}