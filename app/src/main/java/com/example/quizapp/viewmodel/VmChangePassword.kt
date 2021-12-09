package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.client.KtorClientAuth
import com.example.quizapp.model.ktor.responses.ChangePasswordResponse
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.viewmodel.VmChangePassword.ChangePasswordEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmChangePassword @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val auth: KtorClientAuth,
    private val state: SavedStateHandle
): ViewModel() {

    private val changePasswordEventChannel = Channel<ChangePasswordEvent>()

    val changePasswordEventChannelFlow = changePasswordEventChannel.receiveAsFlow()

    private var _currentPw = state.get<String>(CURRENT_PW_KEY) ?: ""
        set(value) {
            state.set(CURRENT_PW_KEY, value)
            field = value
        }

    val currentPw get() = _currentPw

    private var _newPw = state.get<String>(NEW_PW_KEY) ?: ""
        set(value) {
            state.set(NEW_PW_KEY, value)
            field = value
        }

    val newPw get() = _newPw


    fun onCurrentPwChanged(currentPw: String){
        _currentPw = currentPw
    }

    fun onNewPasswordChanged(newPw: String){
        _newPw = newPw
    }

    fun onConfirmButtonClicked() = launch(IO) {
        if(currentPw != preferencesRepository.getUserPassword()) {
            changePasswordEventChannel.send(ShowMessageSnackBar(R.string.errorCurrentPasswordIsWrong))
            return@launch
        }

        if(newPw.isBlank()) {
            changePasswordEventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
            return@launch
        }

        changePasswordEventChannel.send(ShowLoadingDialog(R.string.changingPassword))

        runCatching {
            backendRepository.updateUserPassword(newPw)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            changePasswordEventChannel.send(HideLoadingDialog)
        }.onSuccess { response ->
            if(response.responseType == ChangePasswordResponse.ChangePasswordResponseType.SUCCESSFUL) {
                preferencesRepository.updateUserPassword(newPw)
                preferencesRepository.updateJwtToken(response.newToken)
                auth.resetJwtAuth()

                changePasswordEventChannel.send(NavigateBackEvent)
            }
            changePasswordEventChannel.send(ShowMessageSnackBar(response.responseType.messageRes))
        }.onFailure {
            changePasswordEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotChangePassword))
        }
    }


    sealed class ChangePasswordEvent {
        class ShowMessageSnackBar(@StringRes val messageRes: Int): ChangePasswordEvent()
        object NavigateBackEvent : ChangePasswordEvent()
        class ShowLoadingDialog(@StringRes val messageRes: Int) : ChangePasswordEvent()
        object HideLoadingDialog : ChangePasswordEvent()
    }

    companion object {
        private const val CURRENT_PW_KEY = "currentPwKey"
        private const val NEW_PW_KEY = "newPwKey"
    }
}