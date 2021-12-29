package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.*
import androidx.paging.*
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse.DeleteUserResponse.*
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers.UserMoreOptionsItem.*
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.*
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.resultdispatcher.requests.ConfirmationRequestType
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmAdminManageUsers.*
import com.example.quizapp.viewmodel.VmAdminManageUsers.ManageUsersEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmAdminManageUsers @Inject constructor(
    private val backendRepository: BackendRepository,
    preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : BaseViewModel<ManageUsersEvent>() {

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

    fun onClearSearchQueryClicked() = launch(IO) {
        if (searchQuery.isNotBlank()) {
            eventChannel.send(ClearSearchQueryEvent)
        }
    }

    fun onFilterButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromManageUsersToUserFilterDialog(selectedRoles.toTypedArray()))
    }

    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onAddUserButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromManageUsersToAddEditUser())
    }

    fun onUserItemClicked(user: User) = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.UserMoreOptionsSelection(user)))
    }

    fun onUserMoreOptionsSelectionResultReceived(result: SelectionResult.UserMoreOptionsSelectionResult) = launch(IO) {
        when (result.selectedItem) {
            CHANGE_ROLE -> navigationDispatcher.dispatch(FromManageUsersToChangeUserRoleDialog(result.calledOnUser))
            DELETE -> navigationDispatcher.dispatch(ToConfirmationDialog(ConfirmationRequestType.DeleteUserConfirmationRequest(result.calledOnUser)))
            VIEW_CREATED_QUESTIONNAIRES -> {
                //TODO -> Browse die questionnaires
            }
        }
    }


    fun onUserRoleSuccessfullyChanged(userId: String, newRole: Role) = launch(IO) {
        eventChannel.send(UpdateUserRoleEvent(userId, newRole))
    }

    fun onDeleteUserConfirmationResultReceived(result: ConfirmationResult.DeleteUserConfirmationResult) = launch(IO) {
        if(!result.confirmed) return@launch

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.deletingUser))

        runCatching {
            backendRepository.deleteUser(result.user.id)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
        }.onSuccess { response ->
            if (response.responseType == DeleteUserResponseType.SUCCESSFUL) {
                eventChannel.send(HideUserEvent(result.user.id))
                eventChannel.send(ShowMessageSnackBarEvent(R.string.userDeleted))
            }
        }.onFailure {
            eventChannel.send(ShowMessageSnackBarEvent(R.string.errorCouldNotDeleteUser))
        }
    }

    fun onMangeUsersFilterUpdateReceived(result: FragmentResult.ManageUsersFilterResult){
        result.selectedRoles.let { selectedRoles ->
            state.set(SELECTED_ROLES_KEY, selectedRoles)
            selectedRolesMutableStateFlow.value = selectedRoles
        }
    }

    sealed class ManageUsersEvent: ViewModelEventMarker {
        class UpdateUserRoleEvent(val userId: String, val newRole: Role) : ManageUsersEvent()
        class HideUserEvent(val userId: String) : ManageUsersEvent()
        class ShowUserEvent(val user: User) : ManageUsersEvent()
        class ShowMessageSnackBarEvent(@StringRes val messageRes: Int) : ManageUsersEvent()
        object ClearSearchQueryEvent : ManageUsersEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val SELECTED_ROLES_KEY = "selectedRolesKey"
    }
}