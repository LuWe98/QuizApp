package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.ktor.BackendResponse.LoginUserResponse.*
import com.example.quizapp.model.ktor.BackendResponse.RegisterUserResponse
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.viewmodel.VmAuth.*
import com.example.quizapp.viewmodel.VmAuth.FragmentAuthEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class VmAuth @Inject constructor(
    private val backendRepository: BackendRepository,
    private val preferencesRepository: PreferencesRepository,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle,
) : BaseViewModel<FragmentAuthEvent>() {

    fun checkIfLoggedIn() = launch(IO) {
        if (preferencesRepository.isUserLoggedIn()) {
            navigationDispatcher.dispatch(FromAuthToHomeScreen)
        } else {
            eventChannel.send(ShowLoginScreen)
        }
    }

    //LOGIN
    private var _currentLoginUserName = state.get<String>(LOGIN_USERNAME) ?: ""
        set(value) {
            state.set(LOGIN_USERNAME, value)
            field = value
        }

    val currentLoginUserName get() = _currentLoginUserName

    private var _currentLoginPassword = state.get<String>(LOGIN_PASSWORD) ?: ""
        set(value) {
            state.set(LOGIN_PASSWORD, value)
            field = value
        }

    val currentLoginPassword get() = _currentLoginPassword

    fun onGoToRegisterButtonClicked() = launch(IO) {
        eventChannel.send(SwitchPage(1))

    }

    fun onLoginButtonClicked() = launch(IO) {
        if (currentLoginUserName.isBlank() || currentLoginPassword.isBlank()) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
            return@launch
        }

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.loggingIn))

        runCatching {
            backendRepository.loginUser(currentLoginUserName, currentLoginPassword)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
        }.onSuccess { response ->
            if (response.responseType == LoginUserResponseType.LOGIN_SUCCESSFUL) {
                User(
                    id = response.userId!!,
                    userName = currentLoginUserName,
                    password = currentLoginPassword,
                    role = response.role!!,
                    lastModifiedTimestamp = response.lastModifiedTimeStamp!!
                ).let { user ->
                    if (user.id != preferencesRepository.getUserId()) {
                        localRepository.deleteAllUserData()
                        preferencesRepository.wipePreferenceData()
                    }
                    preferencesRepository.updateUserCredentials(user)
                    preferencesRepository.updateJwtToken(response.token)
                }
                navigationDispatcher.dispatch(FromAuthToHomeScreen)
            }
            eventChannel.send(ShowMessageSnackBar(response.responseType.messageRes))
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorOccurredWhileLoggingInUser))
        }

    }

    fun onLoginEmailEditTextChanged(newValue: String) {
        _currentLoginUserName = newValue.trim()
    }

    fun onLoginPasswordEditTextChanged(newValue: String) {
        _currentLoginPassword = newValue.trim()
    }


    //REGISTER
    private var _currentRegisterUserName = state.get<String>(REGISTER_USERNAME) ?: ""
        set(value) {
            state.set(REGISTER_USERNAME, value)
            field = value
        }

    val currentRegisterUserName get() = _currentRegisterUserName

    private var _currentRegisterPassword = state.get<String>(REGISTER_PASSWORD) ?: ""
        set(value) {
            state.set(REGISTER_PASSWORD, value)
            field = value
        }

    val currentRegisterPassword get() = _currentRegisterPassword

    private var _currentRegisterPasswordConfirm = state.get<String>(REGISTER_PASSWORD_CONFIRM) ?: ""
        set(value) {
            state.set(REGISTER_PASSWORD_CONFIRM, value)
            field = value
        }

    val currentRegisterPasswordConfirm get() = _currentRegisterPasswordConfirm

    fun onGoToLoginButtonClicked() = launch(IO) {
        eventChannel.send(SwitchPage(0))
    }

    fun onRegisterButtonClicked() = launch(IO) {
        if (currentRegisterUserName.isBlank() ||
            currentRegisterPassword.isBlank() ||
            currentRegisterPasswordConfirm.isBlank()
        ) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
            return@launch
        }

        if (currentRegisterPassword != currentRegisterPasswordConfirm) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorPasswordsDoNotMatch))
            return@launch
        }

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.registering))

        runCatching {
            backendRepository.registerUser(currentRegisterUserName, currentRegisterPassword)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
        }.onSuccess { response ->
            if (response.responseType == RegisterUserResponse.RegisterUserResponseType.REGISTER_SUCCESSFUL) {
                eventChannel.send(SetLoginCredentials(currentRegisterUserName, currentRegisterPassword))
                eventChannel.send(SwitchPage(0))
            }

            eventChannel.send(ShowMessageSnackBar(response.responseType.messageRes))
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorOccurredWhileRegisteringUser))
        }

    }

    fun onRegisterEmailEditTextChanged(newValue: String) {
        _currentRegisterUserName = newValue.trim()
    }

    fun onRegisterPasswordEditTextChanged(newValue: String) {
        _currentRegisterPassword = newValue.trim()
    }

    fun onRegisterPasswordConfirmEditTextChanged(newValue: String) {
        _currentRegisterPasswordConfirm = newValue.trim()
    }


    sealed class FragmentAuthEvent: UiEventMarker {
        data class SwitchPage(val pagePosition: Int) : FragmentAuthEvent()
        object ShowLoginScreen : FragmentAuthEvent()
        data class ShowMessageSnackBar(@StringRes val stringRes: Int) : FragmentAuthEvent()
        data class SetLoginCredentials(val email: String, val password: String) : FragmentAuthEvent()
    }

    companion object {
        private const val LOGIN_USERNAME = "loginUserEmail"
        private const val LOGIN_PASSWORD = "loginUserPassword"
        private const val REGISTER_USERNAME = "registerUserEmail"
        private const val REGISTER_PASSWORD = "registerUserPassword"
        private const val REGISTER_PASSWORD_CONFIRM = "registerUserPasswordConfirm"
    }
}