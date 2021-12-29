package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.FragmentResult.ManageUsersFilterResult
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.NavigateBack
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.ToSelectionDialog
import com.example.quizapp.view.fragments.adminscreens.manageusers.filterselection.BsdfManageUsersFilterSelectionArgs
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.*
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmManageUsersFilterSelection.ManageUsersFilterSelectionEvent
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmManageUsersFilterSelection @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : BaseViewModel<ManageUsersFilterSelectionEvent>() {

    private val args = BsdfManageUsersFilterSelectionArgs.fromSavedStateHandle(state)


    private val selectedRolesMutableStateFlow = state.getMutableStateFlow(SELECTED_ROLES_KEY, args.selectedRoles.asList())

    val selectedRolesStateFlow = selectedRolesMutableStateFlow.asStateFlow()

    private val selectedRoles get() = selectedRolesMutableStateFlow.value


    private val selectedOrderByMutableStateFlow = state.getMutableStateFlow(SELECTED_ORDER_BY_KEY, runBlocking(IO) { preferencesRepository.getManageUsersOrderBy() })

    val selectedOrderByStateFlow = selectedOrderByMutableStateFlow.asStateFlow()

    private val selectedOrderBy get() = selectedOrderByMutableStateFlow.value


    private val selectedOrderAscendingMutableStateFlow =
        state.getMutableStateFlow(SELECTED_ORDER_ASCENDING_KEY, runBlocking(IO) { preferencesRepository.getManageUsersAscendingOrder() })

    val selectedOrderAscendingStateFlow = selectedOrderAscendingMutableStateFlow.asStateFlow()

    private val selectedOrderAscending get() = selectedOrderAscendingMutableStateFlow.value


    fun isRoleSelected(role: Role) = role in selectedRoles


    fun onOrderByCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.ManageUsersOrderBySelection(selectedOrderBy)))
    }

    fun onOrderBySelectionResultReceived(result: SelectionResult.UsersOrderBySelectionResult) {
        state.set(SELECTED_ORDER_BY_KEY, result.selectedItem)
        selectedOrderByMutableStateFlow.value = result.selectedItem
    }

    fun onOrderAscendingCardClicked() {
        state.set(SELECTED_ORDER_ASCENDING_KEY, !selectedOrderAscending)
        selectedOrderAscendingMutableStateFlow.value = !selectedOrderAscending
    }

    fun onRoleChipClicked(role: Role) {
        selectedRoles.toMutableList().apply {
            if (isRoleSelected(role)) {
                if (size == 1) return
                remove(role)
            } else {
                add(role)
            }
            state.set(SELECTED_ROLES_KEY, this)
            selectedRolesMutableStateFlow.value = this
        }
    }

    fun onApplyButtonClicked() = launch(IO) {
        preferencesRepository.updateManageUsersOrderBy(selectedOrderBy)
        preferencesRepository.updateManageUsersAscendingOrder(selectedOrderAscending)
        fragmentResultDispatcher.dispatch(ManageUsersFilterResult(selectedRoles.toSet()))
        navigationDispatcher.dispatch(NavigateBack)
    }

    sealed class ManageUsersFilterSelectionEvent: ViewModelEventMarker

    companion object {
        private const val SELECTED_ORDER_BY_KEY = "selectedOrderByKey"
        private const val SELECTED_ORDER_ASCENDING_KEY = "selectedOrderAscendingKey"
        private const val SELECTED_ROLES_KEY = "selectedRolesKey"
    }
}