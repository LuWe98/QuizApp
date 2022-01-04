package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmLocalQuestionnaireFilterSelection.LocalQuestionnaireFilterSelectionEvent
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmLocalQuestionnaireFilterSelection @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : BaseViewModel<LocalQuestionnaireFilterSelectionEvent>() {

    private val selectedOrderByMutableStateFlow = state.getMutableStateFlow(SELECTED_ORDER_BY_KEY, runBlocking(IO) {
        preferencesRepository.getLocalQuestionnaireOrderBy()
    })

    val selectedOrderByStateFlow = selectedOrderByMutableStateFlow.asStateFlow()

    private val selectedOrderBy get() = selectedOrderByMutableStateFlow.value


    private val selectedOrderAscendingMutableStateFlow = state.getMutableStateFlow(SELECTED_ORDER_ASCENDING_KEY, runBlocking(IO) {
        preferencesRepository.getLocalAscendingOrder()
    })

    val selectedOrderAscendingStateFlow = selectedOrderAscendingMutableStateFlow.asStateFlow()

    private val selectedOrderAscending get() = selectedOrderAscendingMutableStateFlow.value


    private val selectedAuthorIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_AUTHORS_KEY, runBlocking(IO) {
        preferencesRepository.getLocalFilteredAuthorIds()
    })

    val selectedAuthorsStateFlow = selectedAuthorIdsMutableStateFlow.map {
        localRepository.getAuthorInfosWithIds(it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val selectedAuthorIds get() = selectedAuthorIdsMutableStateFlow.value


    private val selectedHideCompletedMutableStateFlow = state.getMutableStateFlow(SELECTED_HIDE_COMPLETED_KEY, runBlocking(IO) {
        preferencesRepository.getLocalFilterHideCompleted()
    })

    val selectedHideCompletedStateFlow = selectedHideCompletedMutableStateFlow.asStateFlow()

    private val selectedHideCompleted get() = selectedHideCompletedMutableStateFlow.value


    private val selectedCosIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_COS_IDS_KEY, runBlocking(IO) {
        preferencesRepository.getLocalFilteredCosIds()
    })

    val selectedCosStateFlow = selectedCosIdsMutableStateFlow.map {
        localRepository.getCoursesOfStudiesWithIds(it)
    }.distinctUntilChanged()

    private val selectedCosIds get() = selectedCosIdsMutableStateFlow.value


    private val selectedFacultyIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_FACULTY_IDS_KEY, runBlocking(IO) {
        preferencesRepository.getLocalFilteredFacultyIds()
    })

    val selectedFacultyStateFlow = selectedFacultyIdsMutableStateFlow.map {
        localRepository.getFacultiesWithIds(it.toList())
    }.distinctUntilChanged()

    private val selectedFacultiesIds get() = selectedFacultyIdsMutableStateFlow.value


    fun removeFilteredAuthor(author: AuthorInfo) {
        selectedAuthorIds.toMutableSet().apply {
            remove(author.userId)
            state.set(SELECTED_AUTHORS_KEY, this)
            selectedAuthorIdsMutableStateFlow.value = this
        }
    }

    fun removeFilteredCourseOfStudies(courseOfStudies: CourseOfStudies) {
        selectedCosIds.toMutableSet().apply {
            remove(courseOfStudies.id)
            state.set(SELECTED_COS_IDS_KEY, this)
            selectedCosIdsMutableStateFlow.value = this
        }
    }

    fun removeFilteredFaculty(faculty: Faculty) {
        selectedFacultiesIds.toMutableSet().apply {
            remove(faculty.id)
            state.set(SELECTED_AUTHORS_KEY, this)
            selectedFacultyIdsMutableStateFlow.value = this
        }
    }

    fun onAuthorAddButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToLocalAuthorSelectionDialog(selectedAuthorIds))
    }

    fun onFacultyCardAddButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToFacultySelectionDialog(selectedFacultiesIds))
    }

    fun onCourseOfStudiesAddButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToCourseOfStudiesSelectionDialog(selectedCosIds))
    }

    fun onOrderByCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.LocalOrderBySelection(selectedOrderBy)))
    }

    fun onLocalOrderBySelectionResultReceived(result: SelectionResult.LocalOrderBySelectionResult) {
        state.set(SELECTED_ORDER_BY_KEY, result.selectedItem)
        selectedOrderByMutableStateFlow.value = result.selectedItem
    }

    fun onAuthorsSelectionResultReceived(result: FragmentResult.LocalAuthorSelectionResult) {
        result.authorIds.toSet().let {
            state.set(SELECTED_AUTHORS_KEY, it)
            selectedAuthorIdsMutableStateFlow.value = it
        }
    }

    fun onFacultiesSelectionResultReceived(result: FragmentResult.FacultySelectionResult) {
        result.facultyIds.toSet().apply {
            state.set(SELECTED_FACULTY_IDS_KEY, this)
            selectedFacultyIdsMutableStateFlow.value = this
        }
    }

    fun onCourseOfStudiesSelectionResultReceived(result: FragmentResult.CourseOfStudiesSelectionResult) {
        result.courseOfStudiesIds.toSet().apply {
            state.set(SELECTED_COS_IDS_KEY, this)
            selectedCosIdsMutableStateFlow.value = this
        }
    }

    fun onOrderAscendingCardClicked() {
        state.set(SELECTED_ORDER_ASCENDING_KEY, !selectedOrderAscending)
        selectedOrderAscendingMutableStateFlow.value = !selectedOrderAscending
    }

    fun onHideCompletedCardClicked() {
        state.set(SELECTED_HIDE_COMPLETED_KEY, !selectedHideCompleted)
        selectedHideCompletedMutableStateFlow.value = !selectedHideCompleted
    }

    fun onCollapseButtonClicked() =  launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onApplyButtonClicked() = launch(IO) {
        preferencesRepository.updateLocalFilters(
            selectedOrderBy,
            selectedOrderAscending,
            selectedAuthorIds,
            selectedCosIds,
            selectedFacultiesIds,
            selectedHideCompleted
        )
        navigationDispatcher.dispatch(NavigateBack)
    }

    sealed class LocalQuestionnaireFilterSelectionEvent: UiEventMarker

    companion object {
        private const val SELECTED_ORDER_BY_KEY = "selectedOrderByKey"
        private const val SELECTED_ORDER_ASCENDING_KEY = "selectedOrderAscendingKey"
        private const val SELECTED_AUTHORS_KEY = "selectedAuthorIdsKey"
        private const val SELECTED_HIDE_COMPLETED_KEY = "selectedHideCompletedKey"
        private const val SELECTED_COS_IDS_KEY = "selectedCosIdsKey"
        private const val SELECTED_FACULTY_IDS_KEY = "selectedFacultiesIdsKey"
    }
}