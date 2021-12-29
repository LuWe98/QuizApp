package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.view.NavigationDispatcher
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.addeditquestionnairescreen.DfAddEditAnswerArgs
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class VmAddEditAnswer @Inject constructor(
    private val state: SavedStateHandle
) : BaseViewModel<BaseViewModel.EmptyEventClass>() {

    private val args = DfAddEditAnswerArgs.fromSavedStateHandle(state)

    private val parsedAnswerText get() = args.answer?.answerText ?: ""

    private val parsedIsAnswerCorrect get() = args.answer?.isAnswerCorrect ?: false

    val titleTextRes get() = if(args.answer == null) R.string.addAnswer else R.string.editAnswer



    private val isAnswerCorrectMutableStateFlow = state.getMutableStateFlow(IS_ANSWER_CORRECT_KEY, parsedIsAnswerCorrect)

    val isAnswerCorrectStateFlow = isAnswerCorrectMutableStateFlow.asStateFlow()

    private val isAnswerCorrect get() = isAnswerCorrectMutableStateFlow.value

    private var _answerText = state.get<String>(ANSWER_TEXT_KEY) ?: parsedAnswerText
        set(value) {
            state.set(ANSWER_TEXT_KEY, value)
            field = value
        }

    val answerText get() = _answerText


    fun onConfirmButtonClicked() = launch(IO) {
        val updatedAnswer = args.answer?.copy(
            answerText = answerText,
            isAnswerCorrect =  isAnswerCorrect
        ) ?: Answer(
            answerText = answerText,
            isAnswerCorrect =  isAnswerCorrect
        )

        fragmentResultDispatcher.dispatch(FragmentResult.AddEditAnswerResult(updatedAnswer))
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onCancelButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onIsAnswerCorrectCardClicked(){
        state.set(ANSWER_TEXT_KEY, !isAnswerCorrect)
        isAnswerCorrectMutableStateFlow.value = !isAnswerCorrect
    }

    fun onAnswerTextChanged(newText: String) {
        _answerText = newText
    }

    companion object {
        private const val IS_ANSWER_CORRECT_KEY = "isAnswerCorrectKey"
        private const val ANSWER_TEXT_KEY = "answerTextKey"
    }
}