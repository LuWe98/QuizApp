package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.cachedIn
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigUtil
import com.example.quizapp.model.ktor.paging.PagingUiState
import com.example.quizapp.model.ktor.paging.SimplePagingSource
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.FragmentResult
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.NavigateBack
import com.example.quizapp.view.fragments.dialogs.authorselection.BsdfRemoteAuthorSelectionArgs
import com.example.quizapp.viewmodel.VmRemoteAuthorSelection.RemoteAuthorSelectionEvent
import com.example.quizapp.viewmodel.VmRemoteAuthorSelection.RemoteAuthorSelectionEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.VmRemoteAuthorSelection.RemoteAuthorSelectionEvent.NewPagingUiStateEvent
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
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
) : EventViewModel<RemoteAuthorSelectionEvent>() {

    private val args = BsdfRemoteAuthorSelectionArgs.fromSavedStateHandle(state)

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    private val selectedAuthorsMutableStateFlow = state.getMutableStateFlow(SELECTED_AUTHORS, args.selectedAuthors.toList())

    val selectedAuthorsStateFlow = selectedAuthorsMutableStateFlow.asStateFlow()

    private val selectedAuthors get() = selectedAuthorsMutableStateFlow.value

    private var previousPagerRefreshState: LoadState? = null


    val filteredPagedDataStateFlow = searchQueryMutableStateFlow.flatMapLatest { query ->
        Pager(
            config = PagingConfigUtil.defaultPagingConfig,
            pagingSourceFactory = {
                SimplePagingSource { page ->
                    backendRepository.userApi.getPagedAuthors(
                        limit = PagingConfigUtil.DEFAULT_LIMIT,
                        page = page,
                        searchString = query
                    )
                }
            }
        ).flow
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

    fun onLoadStateChanged(loadStates: CombinedLoadStates, itemCount: Int) = launch(IO) {
        PagingUiState.fromCombinedLoadStates(
            loadStates = loadStates,
            previousLoadState = previousPagerRefreshState,
            itemCount = itemCount,
            isFilteredAction = searchQuery::isNotEmpty
        ).also { state ->
            state?.let(::NewPagingUiStateEvent)?.let {
                eventChannel.send(it)
            }
        }
        previousPagerRefreshState = loadStates.source.refresh
    }

    sealed class RemoteAuthorSelectionEvent: UiEventMarker {
        object ClearSearchQueryEvent : RemoteAuthorSelectionEvent()
        class NewPagingUiStateEvent(val state: PagingUiState) : RemoteAuthorSelectionEvent()

    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_AUTHORS = "selectedAuthorsKey"
    }
}