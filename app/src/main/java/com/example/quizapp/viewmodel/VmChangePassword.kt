package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse.ChangePasswordResponse.*
import com.example.quizapp.model.ktor.client.KtorClientAuth
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.viewmodel.VmChangePassword.*
import com.example.quizapp.viewmodel.VmChangePassword.ChangePasswordEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class VmChangePassword @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val auth: KtorClientAuth,
    private val state: SavedStateHandle
): EventViewModel<ChangePasswordEvent>() {

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

    fun onCancelButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onConfirmButtonClicked() = launch(IO) {
        if(currentPw != preferencesRepository.getUserPassword()) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorCurrentPasswordIsWrong))
            return@launch
        }

        if(newPw.isBlank()) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
            return@launch
        }

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.changingPassword))

        runCatching {
            backendRepository.updateUserPassword(newPw)
        }.also {
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
        }.onSuccess { response ->
            if(response.responseType == ChangePasswordResponseType.SUCCESSFUL) {
                preferencesRepository.updateUserPassword(newPw)
                preferencesRepository.updateJwtToken(response.newToken)
                auth.resetJwtAuth()

                navigationDispatcher.dispatch(NavigateBack)
            }
            eventChannel.send(ShowMessageSnackBar(response.responseType.messageRes))
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotChangePassword))
        }
    }


    sealed class ChangePasswordEvent: UiEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int): ChangePasswordEvent()
    }

    companion object {
        private const val CURRENT_PW_KEY = "currentPwKey"
        private const val NEW_PW_KEY = "newPwKey"
    }
}