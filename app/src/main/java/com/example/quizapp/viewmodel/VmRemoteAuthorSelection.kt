package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.cachedIn
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.utils.RemoteDataAvailability
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.NavigateBack
import com.example.quizapp.view.fragments.dialogs.authorselection.BsdfRemoteAuthorSelectionArgs
import com.example.quizapp.viewmodel.VmRemoteAuthorSelection.RemoteAuthorSelectionEvent
import com.example.quizapp.viewmodel.VmRemoteAuthorSelection.RemoteAuthorSelectionEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class VmRemoteAuthorSelection @Inject constructor(
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : BaseViewModel<RemoteAuthorSelectionEvent>() {

    private val args = BsdfRemoteAuthorSelectionArgs.fromSavedStateHandle(state)

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    private val selectedAuthorsMutableStateFlow = state.getMutableStateFlow(SELECTED_AUTHORS, args.selectedAuthors.toList())

    val selectedAuthorsStateFlow = selectedAuthorsMutableStateFlow.asStateFlow()

    private val selectedAuthors get() = selectedAuthorsMutableStateFlow.value


    val filteredPagedDataStateFlow = searchQueryMutableStateFlow.flatMapLatest { query ->
        PagingConfigValues.getDefaultPager { page ->
            backendRepository.getPagedAuthors(
                page = page,
                searchString = query
            )
        }.flow
    }.cachedIn(viewModelScope)


    fun isAuthorSelected(author: AuthorInfo) = author in selectedAuthors

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onDeleteSearchQueryClicked() = launch(IO) {
        if (searchQuery.isNotEmpty()) {
            eventChannel.send(ClearSearchQueryEvent)
        }
    }

    fun onAuthorClicked(author: AuthorInfo) {
        selectedAuthors.toMutableList().apply {
            if (author in this) {
                remove(author)
            } else {
                add(author)
            }

            state.set(SELECTED_AUTHORS, this)
            selectedAuthorsMutableStateFlow.value = this
        }
    }

    fun onCollapseButtonClicked() =  launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onConfirmButtonClicked() = launch(IO) {
        fragmentResultDispatcher.dispatch(FragmentResult.RemoteAuthorSelectionResult(selectedAuthors))
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onListLoadStateChanged(loadStates: CombinedLoadStates, itemCount: Int) = launch(IO) {
        if (loadStates.append.endOfPaginationReached) {
            if (itemCount == 0 && searchQuery.isNotEmpty()) {
                eventChannel.send(ChangeResultLayoutVisibility(RemoteDataAvailability.NO_ENTRIES_FOUND))
                return@launch
            } else if(itemCount == 0 && searchQuery.isEmpty()) {
                eventChannel.send(ChangeResultLayoutVisibility(RemoteDataAvailability.NO_ENTRIES_EXIST))
                return@launch
            }
        }

        if(itemCount != 0) {
            eventChannel.send(ChangeResultLayoutVisibility(RemoteDataAvailability.ENTRIES_FOUND))
        }
    }

    sealed class RemoteAuthorSelectionEvent: UiEventMarker {
        object ClearSearchQueryEvent : RemoteAuthorSelectionEvent()
        class ChangeResultLayoutVisibility(val state: RemoteDataAvailability) : RemoteAuthorSelectionEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_AUTHORS = "selectedAuthorsKey"
    }
}