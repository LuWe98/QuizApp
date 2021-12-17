package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.FragmentResult.CourseOfStudiesSelectionResult
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.NavigateBack
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.BsdfCourseOfStudiesSelectionArgs
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection.CourseOfStudiesSelectionEvent
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection.CourseOfStudiesSelectionEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmCourseOfStudiesSelection @Inject constructor(
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : BaseViewModel<CourseOfStudiesSelectionEvent>() {

    private val args = BsdfCourseOfStudiesSelectionArgs.fromSavedStateHandle(state)

    private val selectedCoursesOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_COURSES_OF_STUDIES_KEYS, args.selectedCourseOfStudiesIds.toMutableSet())

    val selectedCoursesOfStudiesIdsStateFlow = selectedCoursesOfStudiesIdsMutableStateFlow.asStateFlow()

    private val selectedCoursesOfStudiesIds get() = selectedCoursesOfStudiesIdsMutableStateFlow.value


    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    val facultyFlow = localRepository.allFacultiesFlow.mapNotNull { it }

    fun isCourseOfStudySelected(courseOfStudiesId: String) = selectedCoursesOfStudiesIds.contains(courseOfStudiesId)

    fun getCourseOfStudiesFlow(facultyId: String) = searchQueryMutableStateFlow.flatMapLatest { query ->
        localRepository.getCoursesOfStudiesAssociatedWithFacultyFlow(facultyId, query)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onItemClicked(courseOfStudiesId: String) {
        val updatedSet = mutableSetOf<String>().apply {
            addAll(selectedCoursesOfStudiesIds)
            if (isCourseOfStudySelected(courseOfStudiesId)) {
                remove(courseOfStudiesId)
            } else {
                add(courseOfStudiesId)
            }
        }

        state.set(SELECTED_COURSES_OF_STUDIES_KEYS, updatedSet)
        selectedCoursesOfStudiesIdsMutableStateFlow.value = updatedSet
    }


    fun onConfirmButtonClicked() = launch(IO) {
        fragmentResultDispatcher.dispatch(CourseOfStudiesSelectionResult(selectedCoursesOfStudiesIds.toList()))
        navigationDispatcher.dispatch(NavigateBack)
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


    sealed class CourseOfStudiesSelectionEvent: ViewModelEventMarker {
        object ClearSearchQueryEvent : CourseOfStudiesSelectionEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_COURSES_OF_STUDIES_KEYS = "selectedCoursesOfStudiesKeys"
    }
}