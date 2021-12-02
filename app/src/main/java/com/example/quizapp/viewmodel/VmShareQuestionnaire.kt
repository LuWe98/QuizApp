package com.example.quizapp.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.app
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.dialogs.sharequestionnaire.DfShareQuestionnaireArgs
import com.example.quizapp.viewmodel.VmShareQuestionnaire.ShareQuestionnaireEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmShareQuestionnaire @Inject constructor(
    private val backendRepository: BackendRepository,
    private val applicationScope: CoroutineScope,
    private val state: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    private val args = DfShareQuestionnaireArgs.fromSavedStateHandle(state)

    private val shareQuestionnaireEventChannel = Channel<ShareQuestionnaireEvent>()

    val shareQuestionnaireEventChannelFlow = shareQuestionnaireEventChannel.receiveAsFlow()

    private var _userName = state.get<String>(USER_NAME_KEY) ?: ""
        set(value) {
            state.set(USER_NAME_KEY, value)
            field = value
        }

    val userName get() = _userName


    fun onShareButtonClicked() = launch(IO, applicationScope) {
        shareQuestionnaireEventChannel.send(ShowLoadingDialog(R.string.sharingQuestionnaire))

        runCatching {
            backendRepository.shareQuestionnaireWithUser(args.questionnaireId, userName, false)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            shareQuestionnaireEventChannel.send(HideLoadingDialog)
            shareQuestionnaireEventChannel.send(NavigateBackEvent)
        }.onSuccess { response ->
            shareQuestionnaireEventChannel.send(ShowMessageSnackBar(response.responseType.getMessage(userName, app)))
        }.onFailure {
            shareQuestionnaireEventChannel.send(ShowMessageSnackBar(app.getString(R.string.errorCouldNotShare)))
        }
    }

    fun onUserNameEditTextChanged(newText: String){
        _userName = newText.trim()
    }

    sealed class ShareQuestionnaireEvent {
        object NavigateBackEvent: ShareQuestionnaireEvent()
        class ShowMessageSnackBar(val message: String): ShareQuestionnaireEvent()
        class ShowLoadingDialog(@StringRes val messageRes: Int): ShareQuestionnaireEvent()
        object HideLoadingDialog: ShareQuestionnaireEvent()
    }

    companion object {
        private const val USER_NAME_KEY = "userNameKey"
    }
}