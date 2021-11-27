package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.view.fragments.dialogs.facultyselection.BsdfFacultySelectionArgs
import com.example.quizapp.viewmodel.VmFacultySelection.FacultySelectionEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmFacultySelection @Inject constructor(
    localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfFacultySelectionArgs.fromSavedStateHandle(state)

    private val facultySelectionEventChannel = Channel<FacultySelectionEvent>()

    val facultySelectionEventChannelFlow = facultySelectionEventChannel.receiveAsFlow()



    private val selectedFacultyIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_FACULTIES_KEY, args.selectedFacultyIds.toMutableSet())

    val selectedFacultyIdsStateFlow = selectedFacultyIdsMutableStateFlow.asStateFlow()

    private val selectedFacultyIds get() = selectedFacultyIdsMutableStateFlow.value



    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value



    val facultyFlow = searchQueryMutableStateFlow.map {
        localRepository.findFacultiesWithName(it)
    }.distinctUntilChanged()


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

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onDeleteSearchClicked(){
        if(searchQuery.isNotBlank()){
            launch {
                facultySelectionEventChannel.send(ClearSearchQueryEvent)
            }
        }
    }

    fun onConfirmButtonClicked() {
        launch(IO) {
            facultySelectionEventChannel.send(ConfirmationEvent(selectedFacultyIds.toTypedArray()))
        }
    }


    sealed class FacultySelectionEvent {
        class ConfirmationEvent(val facultyIds: Array<String>) : FacultySelectionEvent()
        object ClearSearchQueryEvent: FacultySelectionEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_FACULTIES_KEY = "selectedFacultyIdsKey"
    }
}