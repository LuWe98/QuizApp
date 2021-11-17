package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.BsdfCourseOfStudiesSelectionArgs
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection.CourseOfStudiesSelectionEvent.ConfirmationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmCourseOfStudiesSelection @Inject constructor(
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfCourseOfStudiesSelectionArgs.fromSavedStateHandle(state)

    private var selectedCoursesOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_COURSES_OF_STUDIES_KEYS, args.selectedCourseOfStudiesIds.toMutableSet())

    val selectedCoursesOfStudiesIdsStateFlow = selectedCoursesOfStudiesIdsMutableStateFlow.asStateFlow()

    private val selectedCoursesOfStudiesIds get() = selectedCoursesOfStudiesIdsMutableStateFlow.value

    private val courseOfStudiesSelectionEventChannel = Channel<CourseOfStudiesSelectionEvent>()

    val courseOfStudiesSelectionEventChannelFlow = courseOfStudiesSelectionEventChannel.receiveAsFlow()

    val facultyFlow = localRepository.allFacultiesFlow.mapNotNull { it }

    fun isCourseOfStudySelected(courseOfStudiesId: String) = selectedCoursesOfStudiesIds.contains(courseOfStudiesId)

    suspend fun getFacultyWithCourseOfStudies(facultyId: String) = localRepository.getFacultyWithCourseOfStudies(facultyId)

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


    fun onConfirmButtonClicked() {
        launch(IO) {
            courseOfStudiesSelectionEventChannel.send(
                ConfirmationEvent(selectedCoursesOfStudiesIds.toTypedArray())
            )
        }
    }


    sealed class CourseOfStudiesSelectionEvent {
        class ConfirmationEvent(val courseOfStudiesIds: Array<String>) : CourseOfStudiesSelectionEvent()
    }

    companion object {
        private const val SELECTED_COURSES_OF_STUDIES_KEYS = "selectedCoursesOfStudiesKeys"
    }
}