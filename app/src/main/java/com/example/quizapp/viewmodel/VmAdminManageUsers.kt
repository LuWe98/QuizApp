package com.example.quizapp.viewmodel

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.lifecycle.*
import androidx.paging.*
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.ktor.responses.DeleteUserResponse.*
import com.example.quizapp.model.menus.datawrappers.UserMoreOptionsItem
import com.example.quizapp.model.menus.datawrappers.UserMoreOptionsItem.*
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmAdminManageUsers.ManageUsersEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmAdminManageUsers @Inject constructor(
    private val backendRepository: BackendRepository,
    preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val manageUsersEventChannel = Channel<ManageUsersEvent>()

    val manageUsersEventChannelFlow = manageUsersEventChannel.receiveAsFlow()

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
                manageUsersEventChannel.send(ClearSearchQueryEvent)
            }
        }
    }

    fun onFilterButtonClicked(){
        launch {
            manageUsersEventChannel.send(NavigateToManageUserSelectionEvent(selectedRoles.toTypedArray()))
        }
    }


    fun onUserItemClicked(user: User) {
        launch(IO) {
            manageUsersEventChannel.send(NavigateToSelectionScreen(SelectionType.UserMoreOptionsSelection(user)))
        }
    }

    fun onMoreOptionsItemSelected(item: UserMoreOptionsItem, type: SelectionType.UserMoreOptionsSelection) {
        launch(IO) {
            when (item) {
                CHANGE_ROLE -> manageUsersEventChannel.send(NavigateToChangeUserRoleDialogEvent(type.user))
                DELETE -> manageUsersEventChannel.send(NavigateToDeletionConfirmationEvent(type.user))
                VIEW_CREATED_QUESTIONNAIRES -> {
                    //TODO -> Browse die questionnaires
                }
            }
        }
    }


    fun onUserRoleSuccessfullyChanged(userId: String, newRole: Role) = launch(IO) {
        manageUsersEventChannel.send(UpdateUserRoleEvent(userId, newRole))
    }

    fun onDeleteUserConfirmed(deleteConfirmation: ConfirmationType.DeleteUserConfirmation) = launch(IO) {
        manageUsersEventChannel.send(ShowLoadingDialog(R.string.deletingUser))
        runCatching {
            backendRepository.deleteUser(deleteConfirmation.user.id)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            manageUsersEventChannel.send(HideLoadingDialog)
        }.onSuccess { response ->
            if (response.responseType == DeleteUserResponseType.SUCCESSFUL) {
                manageUsersEventChannel.send(HideUserEvent(deleteConfirmation.user.id))
                manageUsersEventChannel.send(ShowMessageSnackBarEvent(R.string.userDeleted))
            }
        }.onFailure {
            manageUsersEventChannel.send(ShowMessageSnackBarEvent(R.string.errorCouldNotDeleteUser))
        }
    }

    @Suppress("UNCHECKED_CAST")
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


    sealed class ManageUsersEvent {
        class UpdateUserRoleEvent(val userId: String, val newRole: Role) : ManageUsersEvent()
        class HideUserEvent(val userId: String) : ManageUsersEvent()
        class ShowUserEvent(val user: User) : ManageUsersEvent()
        class NavigateToSelectionScreen(val selectionType: SelectionType) : ManageUsersEvent()
        class NavigateToChangeUserRoleDialogEvent(val user: User) : ManageUsersEvent()
        class NavigateToDeletionConfirmationEvent(val user: User) : ManageUsersEvent()
        class NavigateToManageUserSelectionEvent(val selectedRoles: Array<Role>) : ManageUsersEvent()
        object ClearSearchQueryEvent: ManageUsersEvent()
        class ShowMessageSnackBarEvent(@StringRes val messageRes: Int): ManageUsersEvent()
        class ShowLoadingDialog(@StringRes val messageRes: Int): ManageUsersEvent()
        object HideLoadingDialog: ManageUsersEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_ROLES_KEY = "selectedRolesKey"
    }
}