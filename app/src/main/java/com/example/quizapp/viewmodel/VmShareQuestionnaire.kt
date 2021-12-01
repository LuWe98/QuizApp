package com.example.quizapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.QuizApplication
import com.example.quizapp.R
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.ShareQuestionnaireWithUserResponse.ShareQuestionnaireWithUserResponseType.*
import com.example.quizapp.view.fragments.dialogs.sharequestionnaire.DfShareQuestionnaireArgs
import com.example.quizapp.viewmodel.VmShareQuestionnaire.DfShareQuestionnaireEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VmShareQuestionnaire @Inject constructor(
    private val backendRepository: BackendRepository,
    private val applicationScope: CoroutineScope,
    private val state: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    private val args = DfShareQuestionnaireArgs.fromSavedStateHandle(state)

    val app get() = getApplication<QuizApplication>()

    private val dfShareQuestionnaireEventChannel = Channel<DfShareQuestionnaireEvent>()

    val dfShareQuestionnaireEventChannelFlow = dfShareQuestionnaireEventChannel.receiveAsFlow()

    private var _userName = state.get<String>(USER_NAME_KEY) ?: ""
        set(value) {
            state.set(USER_NAME_KEY, value)
            field = value
        }

    val userName get() = _userName


    fun onShareButtonClicked() = applicationScope.launch(IO) {
        runCatching {
            //TODO -> Wird noch über inputfeld geregelt ob canEdit oder nicht
            backendRepository.shareQuestionnaireWithUser(args.questionnaireId, userName, false)
        }.onFailure {
            dfShareQuestionnaireEventChannel.send(NavigateBackEvent)
            dfShareQuestionnaireEventChannel.send(ShowMessageSnackBar(app.getString(R.string.errorCouldNotShare)))
        }.onSuccess { response ->
            dfShareQuestionnaireEventChannel.send(NavigateBackEvent)
            when(response.responseType){
                SUCCESSFUL -> {
                    dfShareQuestionnaireEventChannel.send(ShowMessageSnackBar(app.getString(R.string.sharedWithUser, userName)))
                }
                USER_DOES_NOT_EXIST -> {
                    dfShareQuestionnaireEventChannel.send(ShowMessageSnackBar(app.getString(R.string.errorUserDoesNotExist)))
                }
                NOT_ACKNOWLEDGED -> {
                    dfShareQuestionnaireEventChannel.send(ShowMessageSnackBar( app.getString(R.string.errorCouldNotShare)))
                }
                ALREADY_SHARED_WITH_USER -> {

                }
                QUESTIONNAIRE_DOES_NOT_EXIST -> {

                }
            }
        }
    }

    fun onUserNameEditTextChanged(newText: String){
        _userName = newText.trim()
    }

    sealed class DfShareQuestionnaireEvent {
        object NavigateBackEvent: DfShareQuestionnaireEvent()
        class ShowMessageSnackBar(val message: String): DfShareQuestionnaireEvent()
    }

    companion object {
        private const val USER_NAME_KEY = "userNameKey"
    }
}