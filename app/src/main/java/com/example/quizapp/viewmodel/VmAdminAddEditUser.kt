package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.CreateUserResponse.CreateUserResponseType
import com.example.quizapp.view.fragments.adminscreens.manageusers.FragmentAdminAddEditUserArgs
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.example.quizapp.viewmodel.VmAdminAddEditUser.AddEditUserEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmAdminAddEditUser @Inject constructor(
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val addEditUserEventChannel = Channel<AddEditUserEvent>()

    val addEditUserEventChannelFlow = addEditUserEventChannel.receiveAsFlow()


    private val args = FragmentAdminAddEditUserArgs.fromSavedStateHandle(state)

    val pageTitleRes get() = if(args.user == null) R.string.addUser else R.string.editUser

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






    fun onUserNameCardClicked(){
        launch(IO) {
            addEditUserEventChannel.send(NavigateToUpdateStringDialog(userName, UpdateStringType.USER_NAME))
        }
    }

    fun onUserPasswordCardClicked() {
        launch(IO) {
            addEditUserEventChannel.send(NavigateToUpdateStringDialog(userPassword, UpdateStringType.USER_PASSWORD))
        }
    }

    fun onUserRoleCardClicked(){
        launch(IO) {
            addEditUserEventChannel.send(NavigateToRoleSelection(userRole))
        }
    }


    fun onUserNameUpdateReceived(newUserName: String) {
        newUserName.trim().let { trimmed ->
            state.set(USER_NAME_KEY, trimmed)
            userNameMutableStateFlow.value = trimmed
        }
    }

    fun onUserPasswordUpdateReceived(newPassword: String) {
        newPassword.trim().let { trimmed ->
            state.set(USER_PASSWORD_KEY, trimmed)
            userPasswordMutableStateFlow.value = trimmed
        }
    }

    fun onUserRoleUpdateReceived(newRole: Role) {
        state.set(USER_ROLE_KEY, newRole)
        userRoleMutableStateFlow.value = newRole
    }


    fun onSaveButtonClicked(){
        launch(IO) {
            if(userName.isEmpty() || userPassword.isEmpty()) {
                addEditUserEventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
                return@launch
            }

            runCatching {
                backendRepository.createUser(userName, userPassword, userRole)
            }.onSuccess { response ->
                addEditUserEventChannel.send(ShowMessageSnackBar(response.responseType.messageRes))

                if(response.responseType == CreateUserResponseType.CREATION_SUCCESSFUL) {
                    addEditUserEventChannel.send(NavigateBackEvent)
                }
            }.onFailure {
                addEditUserEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotCreateUser))
            }
        }
    }


    sealed class AddEditUserEvent {
        class NavigateToUpdateStringDialog(val initialValue: String, val updateType: UpdateStringType): AddEditUserEvent()
        class NavigateToRoleSelection(val currentRole: Role): AddEditUserEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int): AddEditUserEvent()
        object NavigateBackEvent: AddEditUserEvent()
    }

    companion object {
        private const val USER_NAME_KEY = "userNameKey"
        private const val USER_PASSWORD_KEY = "userPasswordKey"
        private const val USER_ROLE_KEY = "userRoleKey"
    }
}