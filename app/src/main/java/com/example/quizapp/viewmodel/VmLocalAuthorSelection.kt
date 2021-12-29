package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.FragmentResult.LocalAuthorSelectionResult
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.NavigateBack
import com.example.quizapp.view.fragments.dialogs.authorselection.local.BsdfLocalAuthorSelectionArgs
import com.example.quizapp.viewmodel.VmLocalAuthorSelection.LocalAuthorSelectionEvent
import com.example.quizapp.viewmodel.VmLocalAuthorSelection.LocalAuthorSelectionEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VmLocalAuthorSelection @Inject constructor(
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : BaseViewModel<LocalAuthorSelectionEvent>() {

    private val args = BsdfLocalAuthorSelectionArgs.fromSavedStateHandle(state)

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    private val selectedAuthorIdsMutableStateFlow = state.getMutableStateFlow(SELECTED_AUTHORS, args.selectedAuthorIds.toSet())

    val selectedAuthorIdsStateFlow = selectedAuthorIdsMutableStateFlow.asStateFlow()

    private val selectedAuthorIds get() = selectedAuthorIdsMutableStateFlow.value


    val filteredAuthorInfos = searchQueryMutableStateFlow.map { query ->
        val authors = localRepository.getAuthorInfosWithName(query)
        preferencesRepository.getOwnAuthorInfo().let { ownAuthorInfo ->
            if (ownAuthorInfo !in authors && ownAuthorInfo.userName.lowercase().contains(query)) {
                authors.toMutableList().apply {
                    add(ownAuthorInfo)
                }.sortedBy(AuthorInfo::userName)
            } else {
                authors
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun isAuthorSelected(author: AuthorInfo) = author.userId in selectedAuthorIds

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onDeleteSearchQueryClicked() = launch(IO) {
        if (searchQuery.isNotBlank()) {
            eventChannel.send(ClearSearchQueryEvent)
        }
    }

    fun onAuthorClicked(author: AuthorInfo) {
        selectedAuthorIds.toMutableSet().apply {
            if (author.userId in this) {
                remove(author.userId)
            } else {
                add(author.userId)
            }
            state.set(SELECTED_AUTHORS, this)
            selectedAuthorIdsMutableStateFlow.value = this
        }
    }

    fun onConfirmButtonClicked() = launch(IO) {
        fragmentResultDispatcher.dispatch(LocalAuthorSelectionResult(selectedAuthorIds.toList()))
        navigationDispatcher.dispatch(NavigateBack)
    }

    sealed class LocalAuthorSelectionEvent: ViewModelEventMarker {
        object ClearSearchQueryEvent : LocalAuthorSelectionEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_AUTHORS = "selectedAuthorsKey"
    }
}