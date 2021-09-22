package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.R
import com.example.quizapp.extensions.containsWhiteSpaces
import com.example.quizapp.extensions.isValidEmail
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.BackendResponse.*
import com.example.quizapp.model.ktor.responses.BackendResponse.RegisterResponse.*
import com.example.quizapp.viewmodel.VmAuth.FragmentAuthEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmAuth @Inject constructor(
    private val backendRepository: BackendRepository,
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val fragmentEventChannel = Channel<FragmentAuthEvent>()

    val fragmentEventChannelFlow get() = fragmentEventChannel.receiveAsFlow()

    fun checkIfLoggedIn() {
        if (preferencesRepository.getUserCredentials().isValid) {
            launch {
                fragmentEventChannel.send(NavigateToHomeScreen)
            }
        }
    }

    //LOGIN STUFF
    var currentLoginEmail = state.get<String>(LOGIN_EMAIL) ?: ""
        set(value) {
            state.set(LOGIN_EMAIL, value)
            field = value
        }

    var currentLoginPassword = state.get<String>(LOGIN_PASSWORD) ?: ""
        set(value) {
            state.set(LOGIN_PASSWORD, value)
            field = value
        }

    fun onGoToRegisterButtonClicked() {
        launch {
            fragmentEventChannel.send(SwitchPage(1))
        }
    }

    fun onLoginButtonClicked() {
        launch {
            if (currentLoginEmail.isEmpty() || currentLoginPassword.isEmpty()) {
                fragmentEventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
                return@launch
            }

            val loginResponse = try {
                backendRepository.loginUser(currentLoginEmail, currentLoginPassword)
            } catch (e : Exception) {
                fragmentEventChannel.send(ShowMessageSnackBar(R.string.errorOccurredWhileLoggingInUser))
                return@launch
            }

            if(loginResponse.isSuccessful){
                preferencesRepository.updateUserEmail(currentLoginEmail)
                preferencesRepository.updateUserPassword(currentLoginPassword)
                fragmentEventChannel.send(NavigateToHomeScreen)
            }

            fragmentEventChannel.send(ShowMessageSnackBar(loginResponse.loginResponseType.messageRes))
        }
    }

    fun onLoginEmailEditTextChanged(newValue: String) {
        currentLoginEmail = newValue.trim()
    }

    fun onLoginPasswordEditTextChanged(newValue: String) {
        currentLoginPassword = newValue.trim()
    }


    //REGISTER STUFF
    var currentRegisterEmail = state.get<String>(REGISTER_EMAIL) ?: ""
        set(value) {
            state.set(REGISTER_EMAIL, value)
            field = value
        }

    var currentRegisterCourseOfStudies = state.get<String>(REGISTER_COURSE_OF_STUDIES) ?: ""
        set(value) {
            state.set(REGISTER_COURSE_OF_STUDIES, value)
            field = value
        }

    var currentRegisterPassword = state.get<String>(REGISTER_PASSWORD) ?: ""
        set(value) {
            state.set(REGISTER_PASSWORD, value)
            field = value
        }

    var currentRegisterPasswordConfirm = state.get<String>(REGISTER_PASSWORD_CONFIRM) ?: ""
        set(value) {
            state.set(REGISTER_PASSWORD_CONFIRM, value)
            field = value
        }

    fun onGoToLoginButtonClicked() {
        launch {
            fragmentEventChannel.send(SwitchPage(0))
        }
    }

    fun onRegisterButtonClicked() {
        launch {
            if (currentRegisterEmail.isEmpty() ||
                currentRegisterCourseOfStudies.isEmpty() ||
                currentRegisterPassword.isEmpty() ||
                currentRegisterPasswordConfirm.isEmpty()
            ) {
                fragmentEventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
                return@launch
            }

            if (currentRegisterEmail.containsWhiteSpaces ||
                currentRegisterCourseOfStudies.containsWhiteSpaces ||
                currentRegisterPassword.containsWhiteSpaces ||
                currentRegisterPasswordConfirm.containsWhiteSpaces
            ) {
                fragmentEventChannel.send(ShowMessageSnackBar(R.string.errorFieldsContainWhitespaces))
                return@launch
            }

            if (currentRegisterPassword != currentRegisterPasswordConfirm) {
                fragmentEventChannel.send(ShowMessageSnackBar(R.string.errorPasswordsDoNotMatch))
                return@launch
            }

            // TODO --> Check Email
            if (!currentRegisterEmail.isValidEmail) {
                fragmentEventChannel.send(ShowMessageSnackBar(R.string.errorEnterValidEmail))
                return@launch
            }


            val registerResponse = try {
                backendRepository.registerUser(currentRegisterEmail, currentRegisterPassword, currentRegisterCourseOfStudies)
            } catch (e: Exception) {
                fragmentEventChannel.send(ShowMessageSnackBar(R.string.errorOccurredWhileRegisteringUser))
                return@launch
            }

            if (registerResponse.isSuccessful) {
                fragmentEventChannel.send(SetLoginCredentials(currentRegisterEmail, currentRegisterPassword))
                fragmentEventChannel.send(SwitchPage(0))
            }

            fragmentEventChannel.send(ShowMessageSnackBar(registerResponse.registerResponseType.messageRes))
        }
    }

    fun onRegisterEmailEditTextChanged(newValue: String) {
        currentRegisterEmail = newValue.trim()
    }

    fun onRegisterCourseOfStudiesEditTextChanged(newValue: String) {
        currentRegisterCourseOfStudies = newValue.trim()
    }

    fun onRegisterPasswordEditTextChanged(newValue: String) {
        currentRegisterPassword = newValue.trim()
    }

    fun onRegisterPasswordConfirmEditTextChanged(newValue: String) {
        currentRegisterPasswordConfirm = newValue.trim()
    }


    sealed class FragmentAuthEvent {
        data class SwitchPage(val pagePosition: Int) : FragmentAuthEvent()
        object NavigateToHomeScreen : FragmentAuthEvent()
        data class ShowMessageSnackBar(@StringRes val stringRes: Int) : FragmentAuthEvent()
        data class SetLoginCredentials(val email: String, val password : String) : FragmentAuthEvent()
    }


    companion object {
        private const val LOGIN_EMAIL = "loginUserEmail"
        private const val LOGIN_PASSWORD = "loginUserPassword"
        private const val REGISTER_EMAIL = "registerUserEmail"
        private const val REGISTER_COURSE_OF_STUDIES = "registerUserCourseOfStudies"
        private const val REGISTER_PASSWORD = "registerUserPassword"
        private const val REGISTER_PASSWORD_CONFIRM = "registerUserPasswordConfirm"
    }
}