package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.CreateUserResponse.CreateUserResponseType
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.*
import com.example.quizapp.view.fragments.resultdispatcher.UpdateStringValueResult
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.adminscreens.manageusers.FragmentAdminAddEditUserArgs
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmAdminAddEditUser.*
import com.example.quizapp.viewmodel.VmAdminAddEditUser.AddEditUserEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class VmAdminAddEditUser @Inject constructor(
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : BaseViewModel<AddEditUserEvent>() {

    private val args = FragmentAdminAddEditUserArgs.fromSavedStateHandle(state)

    val pageTitleRes get() = if (args.user == null) R.string.addUser else R.string.editUser

    private val parsedUserName get() = args.user?.userName ?: ""

    private val parsedUserPassword get() = args.user?.password ?: ""

    private val parsedUserRole get() = args.user?.role ?: Role.USER


    private val userNameMutableStateFlow = state.getMutableStateFlow(USER_NAME_KEY, parsedUserName)

    val userNameStateFlow = userNameMutableStateFlow.asStateFlow()

    private val userName get() = userNameMutableStateFlow.value


    private val userPasswordMutableStateFlow = state.getMutableStateFlow(USER_PASSWORD_KEY, parsedUserPassword)

    val userPasswordStateFlow = userPasswordMutableStateFlow.asStateFlow()

    private val userPassword get() = userPasswordMutableStateFlow.value


    private val userRoleMutableStateFlow = state.getMutableStateFlow(USER_ROLE_KEY, parsedUserRole)

    val userRoleStateFlow = userRoleMutableStateFlow.asStateFlow()

    private val userRole get() = userRoleMutableStateFlow.value


    fun onUserNameCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToStringUpdateDialog(UpdateStringValueResult.AddEditUserNameUpdateResult(userName)))
    }

    fun onUserPasswordCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToStringUpdateDialog(UpdateStringValueResult.AddEditUserPasswordUpdateResult(userPassword)))
    }

    fun onUserRoleCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.RoleSelection(userRole)))
    }


    fun onUserNameUpdateResultReceived(result: UpdateStringValueResult.AddEditUserNameUpdateResult) {
        result.stringValue.trim().let { trimmed ->
            state.set(USER_NAME_KEY, trimmed)
            userNameMutableStateFlow.value = trimmed
        }
    }

    fun onUserPasswordUpdateResultReceived(result: UpdateStringValueResult.AddEditUserPasswordUpdateResult) {
        result.stringValue.trim().let { trimmed ->
            state.set(USER_PASSWORD_KEY, trimmed)
            userPasswordMutableStateFlow.value = trimmed
        }
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
            backendRepository.createUser(userName, userPassword, userRole)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
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


    sealed class AddEditUserEvent: ViewModelEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int, val attachToActivity: Boolean = false) : AddEditUserEvent()
    }

    companion object {
        private const val USER_NAME_KEY = "userNameKey"
        private const val USER_PASSWORD_KEY = "userPasswordKey"
        private const val USER_ROLE_KEY = "userRoleKey"
    }
}