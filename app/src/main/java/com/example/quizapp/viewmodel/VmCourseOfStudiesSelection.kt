package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.view.fragments.addquestionnairescreen.BsdfCourseOfStudiesSelectionArgs
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection.CourseOfStudiesSelectionEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmCourseOfStudiesSelection @Inject constructor(
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {


    private val args = BsdfCourseOfStudiesSelectionArgs.fromSavedStateHandle(state)

    private var selectedCoursesOfStudiesIds = state.get<MutableSet<String>>(SELECTED_COURSES_OF_STUDIES_KEYS) ?: args.selectedCourseOfStudiesIds.toMutableSet()
        set(value) {
            state.set(SELECTED_COURSES_OF_STUDIES_KEYS, value)
            field = value
        }

    private val courseOfStudiesSelectionEventChannel = Channel<CourseOfStudiesSelectionEvent>()

    val courseOfStudiesSelectionEventChannelFlow = courseOfStudiesSelectionEventChannel.receiveAsFlow()


    val facultyFlow = localRepository.allFacultiesFlow.mapNotNull { it }

    val courseOfStudiesStateFlow = localRepository.allCoursesOfStudiesFlow.mapNotNull { it }

    fun isCourseOfStudySelected(courseOfStudiesId: String) = selectedCoursesOfStudiesIds.contains(courseOfStudiesId)

    suspend fun getFacultyWithCourseOfStudies(facultyId: String) = localRepository.getFacultyWithCourseOfStudies(facultyId)

    fun onItemClicked(courseOfStudiesId: String) {
        selectedCoursesOfStudiesIds.add(courseOfStudiesId)
        launch {
            courseOfStudiesSelectionEventChannel.send(ItemClickedEvent)
        }
    }


    fun onConfirmButtonClicked() {
        launch {
            courseOfStudiesSelectionEventChannel.send(ConfirmationEvent(selectedCoursesOfStudiesIds))
        }
    }


    sealed class CourseOfStudiesSelectionEvent {
        object ItemClickedEvent: CourseOfStudiesSelectionEvent()
        class ConfirmationEvent(val selectedCoursesOfStudiesIds: Set<String>) : CourseOfStudiesSelectionEvent()
    }

    companion object {
        const val SELECTED_COURSES_OF_STUDIES_KEYS = "selectedCoursesOfStudiesKeys"
    }
}