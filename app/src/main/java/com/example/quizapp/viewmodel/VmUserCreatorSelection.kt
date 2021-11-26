package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.ktor.paging.UserPagingSource
import com.example.quizapp.view.fragments.dialogs.usercreatorselection.BsdfUserCreatorSelectionArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmUserCreatorSelection @Inject constructor(
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfUserCreatorSelectionArgs.fromSavedStateHandle(state)

    private val userCreatorSelectionEventChannel = Channel<UserCreatorSelectionEvent>()

    val userCreatorSelectionEventChannelFlow = userCreatorSelectionEventChannel.receiveAsFlow()


    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQuery get() = searchQueryMutableStateFlow.value


    private val selectedUsersMutableStateFlow = state.getMutableStateFlow(SELECTED_USERS, args.selectedUsers.toList())

    val selectedUsersStateFlow = selectedUsersMutableStateFlow.asStateFlow()

    private val selectedUsers get() = selectedUsersMutableStateFlow.value


    //TODO -> General PagingSource, welches einfach nen lambda bekommt und sachen damit sucht
    val filteredPagedDataStateFlow = searchQueryMutableStateFlow.flatMapLatest { query ->
        log("LOL: $query")

        Pager(config = PagingConfig(
            pageSize = PagingConfigValues.PAGE_SIZE,
            maxSize = PagingConfigValues.MAX_SIZE
        ), pagingSourceFactory = {
            UserPagingSource(backendRepository, query)
        }
        ).flow.cachedIn(viewModelScope)
    }


    fun isUserSelected(user: User) = user in selectedUsers

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onUserClicked(user: User) {
        selectedUsers.toMutableList().apply {
            if (user in this) {
                remove(user)
            } else {
                add(user)
            }

            state.set(SELECTED_USERS, this)
            selectedUsersMutableStateFlow.value = this
        }
    }

    fun onConfirmButtonClicked() {
        launch(IO) {
            userCreatorSelectionEventChannel.send(UserCreatorSelectionEvent.SendResultEvent(selectedUsers.toTypedArray()))
        }
    }

    sealed class UserCreatorSelectionEvent {
        class SendResultEvent(val selectedUsers: Array<User>) : UserCreatorSelectionEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_USERS = "selectedUsersKey"
    }
}