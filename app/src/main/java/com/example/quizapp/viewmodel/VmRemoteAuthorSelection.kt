package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.view.fragments.dialogs.authorselection.remote.BsdfRemoteAuthorSelectionArgs
import com.example.quizapp.viewmodel.VmRemoteAuthorSelection.UserCreatorSelectionEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.VmRemoteAuthorSelection.UserCreatorSelectionEvent.SendResultEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmRemoteAuthorSelection @Inject constructor(
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfRemoteAuthorSelectionArgs.fromSavedStateHandle(state)

    private val userCreatorSelectionEventChannel = Channel<UserCreatorSelectionEvent>()

    val userCreatorSelectionEventChannelFlow = userCreatorSelectionEventChannel.receiveAsFlow()


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

    fun onDeleteSearchQueryClicked(){
        if(searchQuery.isNotBlank()) {
            launch {
                userCreatorSelectionEventChannel.send(ClearSearchQueryEvent)
            }
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

    fun onConfirmButtonClicked() {
        launch(IO) {
            userCreatorSelectionEventChannel.send(SendResultEvent(selectedAuthors.toTypedArray()))
        }
    }

    sealed class UserCreatorSelectionEvent {
        class SendResultEvent(val selectedAuthors: Array<AuthorInfo>) : UserCreatorSelectionEvent()
        object ClearSearchQueryEvent: UserCreatorSelectionEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_AUTHORS = "selectedAuthorsKey"
    }
}