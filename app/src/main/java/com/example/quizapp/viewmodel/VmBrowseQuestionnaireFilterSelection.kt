package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.datawrappers.BrowsableOrderBy
import com.example.quizapp.view.fragments.searchscreen.filterselection.BrowseQuestionnaireFilterSelectionResult
import com.example.quizapp.view.fragments.searchscreen.filterselection.BsdfBrowseQuestionnaireFilterSelectionArgs
import com.example.quizapp.viewmodel.VmBrowseQuestionnaireFilterSelection.FilterEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmBrowseQuestionnaireFilterSelection @Inject constructor(
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
): ViewModel() {

    private val args = BsdfBrowseQuestionnaireFilterSelectionArgs.fromSavedStateHandle(state)

    private val searchFilterEventChannel = Channel<FilterEvent>()

    val searchFilterEventChannelFlow = searchFilterEventChannel.receiveAsFlow()



    private val selectedAuthorsMutableStateFlow = state.getMutableStateFlow(SELECTED_USERS_KEY, args.selectedAuthors.toSet())

    val selectedAuthorsStateFlow = selectedAuthorsMutableStateFlow.asStateFlow()

    private val selectedAuthors get() = selectedAuthorsMutableStateFlow.value



    private val selectedCourseOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_COURSE_OF_STUDIES_ID_KEY, args.selectedCourseOfStudiesIds.toSet())

    val selectedCourseOfStudiesStateFlow = selectedCourseOfStudiesIdsMutableStateFlow.map {
        localRepository.getCoursesOfStudiesWithIds(it)
    }.distinctUntilChanged()

    private val selectedCourseOfStudiesIds get() = selectedCourseOfStudiesIdsMutableStateFlow.value



    private val selectedFacultyIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_FACULTY_ID_KEY, args.selectedFacultyIds.toSet())

    val selectedFacultyStateFlow = selectedFacultyIdsMutableStateFlow.map {
        localRepository.getFacultiesWithIds(it.toList())
    }.distinctUntilChanged()

    private val selectedFacultyIds get() = selectedFacultyIdsMutableStateFlow.value



    private val orderByMutableStateFlow = state.getMutableStateFlow(SELECTED_ORDER_BY_KEY, runBlocking(IO) { preferencesRepository.getBrowsableOrderBy() })

    val orderByStateFlow = orderByMutableStateFlow.asStateFlow()

    private val orderBy get() = orderByMutableStateFlow.value



    private val orderAscendingMutableStateFlow = state.getMutableStateFlow(SELECTED_ORDER_ASCENDING_KEY, runBlocking(IO) { preferencesRepository.getBrowsableAscendingOrder() })

    val orderAscendingStateFlow = orderAscendingMutableStateFlow.asStateFlow()

    private val orderAscending get() = orderAscendingMutableStateFlow.value



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

    fun removeFilteredAuthor(author: AuthorInfo) {
        selectedAuthors.toMutableSet().apply {
            remove(author)
            state.set(SELECTED_USERS_KEY, this)
            selectedAuthorsMutableStateFlow.value = this
        }
    }


    fun onOrderByCardClicked() {
        launch(IO) {
            searchFilterEventChannel.send(NavigateToSortBySelection(orderBy))
        }
    }

    fun onOrderAscendingCardClicked(){
        state.set(SELECTED_ORDER_ASCENDING_KEY, !orderAscending)
        orderAscendingMutableStateFlow.value = !orderAscending
    }


    fun onFacultyCardAddButtonClicked() {
        launch(IO) {
            searchFilterEventChannel.send(NavigateToFacultySelectionScreen(selectedFacultyIds.toTypedArray()))
        }
    }

    fun onCourseOfStudiesAddButtonClicked() {
        launch(IO) {
            searchFilterEventChannel.send(NavigateToCourseOfStudiesSelectionScreen(selectedCourseOfStudiesIds.toTypedArray()))
        }
    }

    fun onAuthorAddButtonClicked() {
        launch(IO) {
            searchFilterEventChannel.send(NavigateToUserSelectionScreen(selectedAuthors.toTypedArray()))
        }
    }

    fun onSortByUpdateReceived(browsableOrderBy: BrowsableOrderBy) {
        state.set(SELECTED_ORDER_BY_KEY, browsableOrderBy)
        orderByMutableStateFlow.value = browsableOrderBy
    }

    fun onSelectedAuthorsUpdateReceived(selectedAuthors: Array<AuthorInfo>) {
        selectedAuthors.toSet().let {
            state.set(SELECTED_USERS_KEY, it)
            selectedAuthorsMutableStateFlow.value = it
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

    fun onApplyButtonClicked(){
        launch(IO) {
            preferencesRepository.updateBrowsableOrderBy(orderBy)
            preferencesRepository.updateBrowsableAscendingOrder(orderAscending)
            BrowseQuestionnaireFilterSelectionResult(
                selectedCourseOfStudiesIds,
                selectedFacultyIds,
                selectedAuthors
            ).let {
                searchFilterEventChannel.send(ApplyFilterPreferencesEvent(it))
            }
        }
    }

    sealed class FilterEvent {
        class NavigateToSortBySelection(val browsableOrderBy: BrowsableOrderBy) : FilterEvent()
        class NavigateToUserSelectionScreen(val selectedUsers: Array<AuthorInfo>) : FilterEvent()
        class NavigateToCourseOfStudiesSelectionScreen(val selectedCourseOfStudiesIds: Array<String>) : FilterEvent()
        class NavigateToFacultySelectionScreen(val selectedFacultyIds: Array<String>) : FilterEvent()
        class ApplyFilterPreferencesEvent(val resultBrowse: BrowseQuestionnaireFilterSelectionResult): FilterEvent()
    }

    companion object {
        private const val SELECTED_USERS_KEY = "selectedUsersKeys"
        private const val SELECTED_COURSE_OF_STUDIES_ID_KEY = "selectedCourseOfStudiesKey"
        private const val SELECTED_FACULTY_ID_KEY = "selectedFacultyIdsKey"
        private const val SELECTED_ORDER_BY_KEY = "orderByKey"
        private const val SELECTED_ORDER_ASCENDING_KEY = "orderAscendingKey"
    }
}