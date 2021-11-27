package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.view.fragments.dialogs.usercreatorselection.BsdfUserCreatorSelectionArgs
import com.example.quizapp.viewmodel.VmUserCreatorSelection.UserCreatorSelectionEvent.*
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

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    private val selectedUsersMutableStateFlow = state.getMutableStateFlow(SELECTED_USERS, args.selectedUsers.toList())

    val selectedUsersStateFlow = selectedUsersMutableStateFlow.asStateFlow()

    private val selectedUsers get() = selectedUsersMutableStateFlow.value


    val filteredPagedDataStateFlow = searchQueryMutableStateFlow.flatMapLatest { query ->
        PagingConfigValues.getDefaultPager { page ->
            backendRepository.getPagedCreators(
                page = page,
                searchString = query
            )
        }.flow
    }.cachedIn(viewModelScope)


    fun isUserSelected(user: User) = user in selectedUsers

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
            userCreatorSelectionEventChannel.send(SendResultEvent(selectedUsers.toTypedArray()))
        }
    }

    sealed class UserCreatorSelectionEvent {
        class SendResultEvent(val selectedUsers: Array<User>) : UserCreatorSelectionEvent()
        object ClearSearchQueryEvent: UserCreatorSelectionEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_USERS = "selectedUsersKey"
    }
}