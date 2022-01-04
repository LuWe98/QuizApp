package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.utils.LocalDataAvailability
import com.example.quizapp.utils.asLocalDataAvailability
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.NavigateBack
import com.example.quizapp.view.fragments.dialogs.facultyselection.BsdfFacultySelectionArgs
import com.example.quizapp.viewmodel.VmFacultySelection.FacultySelectionEvent
import com.example.quizapp.viewmodel.VmFacultySelection.FacultySelectionEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmFacultySelection @Inject constructor(
    localRepository: LocalRepository,
    private val state: SavedStateHandle
) : BaseViewModel<FacultySelectionEvent>() {

    private val args = BsdfFacultySelectionArgs.fromSavedStateHandle(state)


    private val selectedFacultyIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_FACULTIES_KEY, args.selectedFacultyIds.toMutableSet())

    val selectedFacultyIdsStateFlow = selectedFacultyIdsMutableStateFlow.asStateFlow()

    private val selectedFacultyIds get() = selectedFacultyIdsMutableStateFlow.value


    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    val facultyFlow = searchQueryMutableStateFlow.flatMapLatest { query ->
        localRepository.findFacultiesWithNameFlow(query).map { list ->
            list.asLocalDataAvailability(query::isNotEmpty)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, LocalDataAvailability.DataFound(emptyList()))


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

    fun onDeleteSearchClicked() = launch(IO) {
        if (searchQuery.isNotEmpty()) {
            eventChannel.send(ClearSearchQueryEvent)
        }
    }

    fun onCollapseButtonClicked() =  launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onConfirmButtonClicked() = launch(IO) {
        fragmentResultDispatcher.dispatch(FragmentResult.FacultySelectionResult(selectedFacultyIds.toList()))
        navigationDispatcher.dispatch(NavigateBack)
    }


    sealed class FacultySelectionEvent: UiEventMarker {
        object ClearSearchQueryEvent : FacultySelectionEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_FACULTIES_KEY = "selectedFacultyIdsKey"
    }
}