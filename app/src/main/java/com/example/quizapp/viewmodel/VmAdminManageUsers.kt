package com.example.quizapp.viewmodel

import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.*
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.ktor.responses.DeleteUserResponse.*
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.menus.UserMoreOptionsItem
import com.example.quizapp.model.menus.UserMoreOptionsItem.*
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmAdminManageUsers.FragmentAdminEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VmAdminManageUsers @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val applicationScope: CoroutineScope,
    private val state: SavedStateHandle
) : ViewModel() {

    private val fragmentAdminEventChannel = Channel<FragmentAdminEvent>()

    val fragmentAdminEventChannelFlow = fragmentAdminEventChannel.receiveAsFlow()

    private val searchQueryMutableStatFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStatFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStatFlow.value



    private val selectedRolesMutableStateFlow = state.getMutableStateFlow(SELECTED_ROLES_KEY, Role.values().toSet())

    private val selectedRoles get() = selectedRolesMutableStateFlow.value


    val filteredPagedDataStateFlow = combine(
        searchQueryMutableStatFlow,
        selectedRolesMutableStateFlow,
        preferencesRepository.manageUsersOrderByFlow,
        preferencesRepository.manageUsersAscendingOrderFlow
    ) { query, roles, orderBy, ascending ->
        PagingConfigValues.getDefaultPager { page ->
            backendRepository.getPagedUsersAdmin(
                page = page,
                searchString = query,
                roles = roles,
                orderBy = orderBy,
                ascending = ascending
            )
        }
    }.flatMapLatest(Pager<Int, User>::flow::get).cachedIn(viewModelScope)


    fun onSearchQueryChanged(query: String) {
        state.set(SEARCH_QUERY_KEY, query)
        searchQueryMutableStatFlow.value = query
    }

    fun onClearSearchQueryClicked(){
        if(searchQuery.isNotBlank()) {
            launch {
                fragmentAdminEventChannel.send(ClearSearchQueryEvent)
            }
        }
    }

    fun onFilterButtonClicked(){
        launch {
            fragmentAdminEventChannel.send(NavigateToManageUserSelectionEvent(selectedRoles.toTypedArray()))
        }
    }


    fun onUserItemClicked(user: User) {
        launch(IO) {
            fragmentAdminEventChannel.send(NavigateToUserMoreOptionsSelection(user))
        }
    }

    fun onMoreOptionsItemSelected(item: UserMoreOptionsItem, type: SelectionType.UserMoreOptionsSelection) {
        launch(IO) {
            when (item) {
                CHANGE_ROLE -> fragmentAdminEventChannel.send(NavigateToChangeUserRoleDialogEvent(type.user))
                DELETE -> fragmentAdminEventChannel.send(NavigateToDeletionConfirmationEvent(type.user))
                VIEW_CREATED_QUESTIONNAIRES -> {
                    //TODO -> Browse die questionnaires
                }
            }
        }
    }


    fun onUserRoleSuccessfullyChanged(userId: String, newRole: Role) = launch(IO) {
        fragmentAdminEventChannel.send(UpdateUserRoleEvent(userId, newRole))
    }

    fun onDeleteUserConfirmed(deleteConfirmation: ConfirmationType.DeleteUserConfirmation) = applicationScope.launch(IO) {
        runCatching {
            backendRepository.deleteUser(deleteConfirmation.user.id)
        }.onSuccess { response ->
            if (response.responseType == DeleteUserResponseType.SUCCESSFUL) {
                fragmentAdminEventChannel.send(HideUserEvent(deleteConfirmation.user.id))
            }
        }
    }

    fun onFilterUpdateReceived(key: String, bundle: Bundle){
        bundle.apply {
            classLoader = Role::class.java.classLoader
            (getParcelableArray(key) as Array<Role>?)?.let {
                it.toSet().let { selectedRoles ->
                    state.set(SELECTED_ROLES_KEY, selectedRoles)
                    selectedRolesMutableStateFlow.value = selectedRoles
                }
            }
        }
    }


    sealed class FragmentAdminEvent {
        class UpdateUserRoleEvent(val userId: String, val newRole: Role) : FragmentAdminEvent()
        class HideUserEvent(val userId: String) : FragmentAdminEvent()
        class ShowUserEvent(val user: User) : FragmentAdminEvent()
        class NavigateToUserMoreOptionsSelection(val user: User) : FragmentAdminEvent()
        class NavigateToChangeUserRoleDialogEvent(val user: User) : FragmentAdminEvent()
        class NavigateToDeletionConfirmationEvent(val user: User) : FragmentAdminEvent()
        class NavigateToManageUserSelectionEvent(val selectedRoles: Array<Role>) : FragmentAdminEvent()
        object ClearSearchQueryEvent: FragmentAdminEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_ROLES_KEY = "selectedRolesKey"
    }
}