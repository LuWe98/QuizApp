package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.fragments.dialogs.facultyselection.BsdfFacultySelectionArgs
import com.example.quizapp.viewmodel.VmFacultySelection.FacultySelectionEvent.ConfirmationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmFacultySelection @Inject constructor(
    localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfFacultySelectionArgs.fromSavedStateHandle(state)

    private var selectedFacultyIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_FACULTIES_KEY, args.selectedFacultyIds.toMutableSet())

    val selectedFacultyIdsStateFlow = selectedFacultyIdsMutableStateFlow.asStateFlow()

    private val selectedFacultyIds get() = selectedFacultyIdsMutableStateFlow.value

    private val facultySelectionEventChannel = Channel<FacultySelectionEvent>()

    val facultySelectionEventChannelFlow = facultySelectionEventChannel.receiveAsFlow()

    val facultyFlow = localRepository.allFacultiesFlow.distinctUntilChanged()

    fun isFacultySelected(courseOfStudiesId: String) = selectedFacultyIds.contains(courseOfStudiesId)

    fun onItemClicked(facultyId: String) {
        val updatedSet = mutableSetOf<String>().apply {
            addAll(selectedFacultyIds)
            if (isFacultySelected(facultyId)) {
                remove(facultyId)
            } else {
                add(facultyId)
            }
        }

        state.set(SELECTED_FACULTIES_KEY, updatedSet)
        selectedFacultyIdsMutableStateFlow.value = updatedSet
    }


    fun onConfirmButtonClicked() {
        launch(IO) {
            facultySelectionEventChannel.send(ConfirmationEvent(selectedFacultyIds.toTypedArray()))
        }
    }


    sealed class FacultySelectionEvent {
        class ConfirmationEvent(val facultyIds: Array<String>) : FacultySelectionEvent()
    }

    companion object {
        private const val SELECTED_FACULTIES_KEY = "selectedFacultyIdsKey"
    }
}