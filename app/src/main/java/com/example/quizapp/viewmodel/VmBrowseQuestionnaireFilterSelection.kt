package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.FragmentResult.*
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.*
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionRequestType
import com.example.quizapp.view.fragments.searchscreen.filterselection.BsdfBrowseQuestionnaireFilterSelectionArgs
import com.example.quizapp.viewmodel.VmBrowseQuestionnaireFilterSelection.FilterEvent
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmBrowseQuestionnaireFilterSelection @Inject constructor(
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : BaseViewModel<FilterEvent>() {

    private val args = BsdfBrowseQuestionnaireFilterSelectionArgs.fromSavedStateHandle(state)

    private val selectedAuthorsMutableStateFlow = state.getMutableStateFlow(SELECTED_AUTHORS_KEY, args.selectedAuthors.toSet())

    val selectedAuthorsStateFlow = selectedAuthorsMutableStateFlow.asStateFlow()

    private val selectedAuthors get() = selectedAuthorsMutableStateFlow.value


    private val selectedCourseOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_COURSE_OF_STUDIES_ID_KEY, runBlocking(IO) {
        preferencesRepository.getBrowsableCosIds()
    })

    val selectedCourseOfStudiesStateFlow = selectedCourseOfStudiesIdsMutableStateFlow.map {
        localRepository.getCoursesOfStudiesWithIds(it)
    }.distinctUntilChanged()

    private val selectedCourseOfStudiesIds get() = selectedCourseOfStudiesIdsMutableStateFlow.value


    private val selectedFacultyIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_FACULTY_ID_KEY, runBlocking(IO) {
        preferencesRepository.getBrowsableFacultyIds()
    })

    val selectedFacultyStateFlow = selectedFacultyIdsMutableStateFlow.map {
        localRepository.getFacultiesWithIds(it.toList())
    }.distinctUntilChanged()

    private val selectedFacultyIds get() = selectedFacultyIdsMutableStateFlow.value


    private val orderByMutableStateFlow = state.getMutableStateFlow(SELECTED_ORDER_BY_KEY, runBlocking(IO) { preferencesRepository.getBrowsableOrderBy() })

    val orderByStateFlow = orderByMutableStateFlow.asStateFlow()

    private val orderBy get() = orderByMutableStateFlow.value


    private val orderAscendingMutableStateFlow =
        state.getMutableStateFlow(SELECTED_ORDER_ASCENDING_KEY, runBlocking(IO) { preferencesRepository.getBrowsableAscendingOrder() })

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
            state.set(SELECTED_AUTHORS_KEY, this)
            selectedAuthorsMutableStateFlow.value = this
        }
    }


    fun onOrderByCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.RemoteOrderBySelection(orderBy)))
    }

    fun onOrderAscendingCardClicked() {
        state.set(SELECTED_ORDER_ASCENDING_KEY, !orderAscending)
        orderAscendingMutableStateFlow.value = !orderAscending
    }


    fun onFacultyCardAddButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToFacultySelectionDialog(selectedFacultyIds.toTypedArray()))
    }

    fun onCourseOfStudiesAddButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToCourseOfStudiesSelectionDialog(selectedCourseOfStudiesIds.toTypedArray()))
    }

    fun onAuthorAddButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToRemoteAuthorSelectionDialog(selectedAuthors.toTypedArray()))
    }

    fun onRemoteOrderBySelectionResultReceived(result: SelectionResult.RemoteOrderBySelectionResult) {
        state.set(SELECTED_ORDER_BY_KEY, result.selectedItem)
        orderByMutableStateFlow.value = result.selectedItem
    }

    fun onAuthorsSelectionResultReceived(result: RemoteAuthorSelectionResult) {
        result.authors.toSet().let {
            state.set(SELECTED_AUTHORS_KEY, it)
            selectedAuthorsMutableStateFlow.value = it
        }
    }

    fun onFacultiesSelectionResultReceived(result: FacultySelectionResult) {
        result.facultyIds.toSet().let {
            state.set(SELECTED_FACULTY_ID_KEY, it)
            selectedFacultyIdsMutableStateFlow.value = it
        }
    }

    fun onCourseOfStudiesSelectionResultReceived(result: CourseOfStudiesSelectionResult) {
        result.courseOfStudiesIds.toSet().let {
            state.set(SELECTED_COURSE_OF_STUDIES_ID_KEY, it)
            selectedCourseOfStudiesIdsMutableStateFlow.value = it
        }
    }

    fun onApplyButtonClicked() = launch(IO) {
        preferencesRepository.updateRemoteFilters(
            orderBy,
            orderAscending,
            selectedCourseOfStudiesIds,
            selectedFacultyIds
        )

        fragmentResultDispatcher.dispatch(RemoteQuestionnaireFilterResult(selectedAuthors))
        navigationDispatcher.dispatch(NavigateBack)
    }

    sealed class FilterEvent: ViewModelEventMarker

    companion object {
        private const val SELECTED_AUTHORS_KEY = "selectedUsersKeys"
        private const val SELECTED_COURSE_OF_STUDIES_ID_KEY = "selectedCourseOfStudiesKey"
        private const val SELECTED_FACULTY_ID_KEY = "selectedFacultyIdsKey"
        private const val SELECTED_ORDER_BY_KEY = "orderByKey"
        private const val SELECTED_ORDER_ASCENDING_KEY = "orderAscendingKey"
    }
}