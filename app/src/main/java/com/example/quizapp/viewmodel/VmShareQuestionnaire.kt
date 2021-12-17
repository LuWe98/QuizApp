package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.QuizApplication
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.dialogs.sharequestionnaire.DfShareQuestionnaireArgs
import com.example.quizapp.viewmodel.VmShareQuestionnaire.ShareQuestionnaireEvent
import com.example.quizapp.viewmodel.VmShareQuestionnaire.ShareQuestionnaireEvent.ShowMessageSnackBar
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class VmShareQuestionnaire @Inject constructor(
    private val backendRepository: BackendRepository,
    private val applicationScope: CoroutineScope,
    private val state: SavedStateHandle,
    private val app: QuizApplication
) : BaseViewModel<ShareQuestionnaireEvent>() {

    private val args = DfShareQuestionnaireArgs.fromSavedStateHandle(state)

    private var _userName = state.get<String>(USER_NAME_KEY) ?: ""
        set(value) {
            state.set(USER_NAME_KEY, value)
            field = value
        }

    val userName get() = _userName


    fun onShareButtonClicked() = launch(IO, applicationScope) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.sharingQuestionnaire))

        runCatching {
            backendRepository.shareQuestionnaireWithUser(args.questionnaireId, userName, false)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
        }.onSuccess { response ->
            eventChannel.send(ShowMessageSnackBar(response.responseType.getMessage(userName, app)))
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(app.getString(R.string.errorCouldNotShare)))
        }

        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onCancelButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onUserNameEditTextChanged(newText: String){
        _userName = newText.trim()
    }

    sealed class ShareQuestionnaireEvent: ViewModelEventMarker {
        class ShowMessageSnackBar(val message: String): ShareQuestionnaireEvent()
    }

    companion object {
        private const val USER_NAME_KEY = "userNameKey"
    }
}