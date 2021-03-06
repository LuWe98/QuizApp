package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendRepositoryImpl
import com.example.quizapp.model.ktor.BackendResponse.CreateUserResponse.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.adminscreens.manageusers.FragmentAdminAddEditUserArgs
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.viewmodel.VmAdminAddEditUser.*
import com.example.quizapp.viewmodel.VmAdminAddEditUser.AddEditUserEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class VmAdminAddEditUser @Inject constructor(
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : EventViewModel<AddEditUserEvent>() {

    private val args = FragmentAdminAddEditUserArgs.fromSavedStateHandle(state)

    val pageTitleRes get() = if (args.user == null) R.string.create else R.string.edit

    private val parsedUserName get() = args.user?.name ?: ""

    private val parsedUserPassword get() = args.user?.password ?: ""

    private val parsedUserRole get() = args.user?.role ?: Role.USER


    private var _userName = state.get<String>(USER_NAME_KEY) ?: parsedUserName
        set(value) {
            state.set(USER_NAME_KEY, value)
            field = value
        }

    val userName get() = _userName


    private var _userPassword = state.get<String>(USER_PASSWORD_KEY) ?: parsedUserPassword
        set(value) {
            state.set(USER_PASSWORD_KEY, value)
            field = value
        }

    val userPassword get() = _userPassword


    private val userRoleMutableStateFlow = state.getMutableStateFlow(USER_ROLE_KEY, parsedUserRole)

    val userRoleStateFlow = userRoleMutableStateFlow.asStateFlow()

    private val userRole get() = userRoleMutableStateFlow.value


    fun onUserNameChanged(newName: String) {
        _userName = newName
    }

    fun onPasswordChanged(newPassword: String) {
        _userPassword = newPassword
    }

    fun onUserRoleCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.RoleSelection(userRole)))
    }

    fun onUserRoleSelectionResultReceived(result: SelectionResult.RoleSelectionResult) {
        state.set(USER_ROLE_KEY, result.selectedItem)
        userRoleMutableStateFlow.value =  result.selectedItem
    }

    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onSaveButtonClicked() = launch(IO) {
        if (userName.isEmpty() || userPassword.isEmpty()) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
            return@launch
        }

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.savingUser))

        runCatching {
            backendRepository.userApi.createUser(userName, userPassword, userRole)
        }.also {
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
        }.onSuccess { response ->
            if (response.responseType == CreateUserResponseType.CREATION_SUCCESSFUL) {
                navigationDispatcher.dispatch(NavigateBack)
            }

            eventChannel.send(
                ShowMessageSnackBar(
                    response.responseType.messageRes,
                    response.responseType == CreateUserResponseType.CREATION_SUCCESSFUL
                )
            )
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotCreateUser))
        }
    }


    sealed class AddEditUserEvent: UiEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int, val attachToActivity: Boolean = false) : AddEditUserEvent()
    }

    companion object {
        private const val USER_NAME_KEY = "userNameKey"
        private const val USER_PASSWORD_KEY = "userPasswordKey"
        private const val USER_ROLE_KEY = "userRoleKey"
    }
}