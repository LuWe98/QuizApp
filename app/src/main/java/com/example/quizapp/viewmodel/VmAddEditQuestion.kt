package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.div
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.indexOfFirstOrNull
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.Question
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.addeditquestionnairescreen.FragmentAddEditQuestionArgs
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.datawrappers.AddEditAnswerMoreOptionsItem
import com.example.quizapp.viewmodel.VmAddEditQuestion.*
import com.example.quizapp.viewmodel.VmAddEditQuestion.FragmentAddEditQuestionEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmAddEditQuestion @Inject constructor(
    private val state: SavedStateHandle
) : EventViewModel<FragmentAddEditQuestionEvent>() {

    private val args = FragmentAddEditQuestionArgs.fromSavedStateHandle(state)

    private val parsedQuestion get() = args.questionWithAnswers?.question ?: Question()

    private val parsedAnswers get() = args.questionWithAnswers?.answers?.sortedBy(Answer::answerPosition) ?: emptyList()

    val pageTitleRes get() = if (args.questionWithAnswers == null) R.string.add else R.string.edit

    private var _questionText = state.get<String>(QUESTION_TEXT_KEY) ?: parsedQuestion.questionText
        set(value) {
            state.set(QUESTION_TEXT_KEY, value)
            field = value
        }

    val questionText get() = _questionText


    private var isQuestionMultipleChoiceMutableStateFlow = state.getMutableStateFlow(QUESTION_TYPE_KEY, parsedQuestion.isMultipleChoice)

    val isQuestionMultipleChoiceStateFlow get() = isQuestionMultipleChoiceMutableStateFlow.asStateFlow()

    private val isQuestionMultipleChoice get() = isQuestionMultipleChoiceStateFlow.value



    private var answersMutableStateFlow = state.getMutableStateFlow(ANSWERS_KEY, parsedAnswers)

    val answersStateFlow = answersMutableStateFlow.map { it.sortedBy(Answer::answerPosition) }

    val answers get() = answersMutableStateFlow.value


    private fun findAnswerIndex(answerId: String) = answers.indexOfFirstOrNull { it.id == answerId }

    private fun setAnswerList(answers: List<Answer>) {
        answers.mapIndexed { index, answer -> answer.copy(answerPosition = index) }.let { updatedAnswers ->
            state.set(ANSWERS_KEY, updatedAnswers)
            answersMutableStateFlow.value = updatedAnswers
        }
    }

    private fun deleteAnswerFromList(answer: Answer) = launch(IO) {
        answers.toMutableList().apply {
            val answerIndex = indexOf(answer)
            removeAt(answerIndex)
            setAnswerList(this)
            eventChannel.send(ShowAnswerDeletedSnackBar(answer, answerIndex))
        }
    }

    private fun addAnswerToList(answer: Answer) {
        answers.toMutableList().apply {
            add(answer)
            setAnswerList(this)
        }
    }

    fun onQuestionTextChanged(newText: String) {
        _questionText = newText
    }


    fun onAnswerClicked(answer: Answer) = launch(IO) {
        navigationDispatcher.dispatch(FromAddEditQuestionToAddEditAnswer(answer))
    }

    fun onAnswerLongClicked(answer: Answer) = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.AddEditAnswerMoreOptionsSelection(answer)))
    }

    fun onAnswerMoreOptionsSelectionResultReceived(result: SelectionResult.AddEditAnswerMoreOptionsSelectionResult) = launch(IO) {
        when (result.selectedItem) {
            AddEditAnswerMoreOptionsItem.EDIT -> navigationDispatcher.dispatch(FromAddEditQuestionToAddEditAnswer(result.calledOnAnswer))
            AddEditAnswerMoreOptionsItem.DELETE -> deleteAnswerFromList(result.calledOnAnswer)
        }
    }

    fun onAddEditAnswerResultReceived(result: FragmentResult.AddEditAnswerResult) = launch(IO) {
        findAnswerIndex(result.answer.id)?.let { index ->
            answers.toMutableList().apply {
                set(index, result.answer)
                setAnswerList(this)
            }
            return@launch
        }
        addAnswerToList(result.answer)
    }


    fun onAnswerCheckClicked(answer: Answer) {
        answers.toMutableList().apply {
            if (!isQuestionMultipleChoice) {
                onEachIndexed { index, answer ->
                    set(index, answer.copy(isAnswerCorrect = false))
                }
            }
            indexOf(answer).let { index ->
                if (index != -1) {
                    set(index, answer.copy(isAnswerCorrect = !answer.isAnswerCorrect))
                }
            }
            setAnswerList(this)
        }
    }


    fun onUndoDeleteAnswerClicked(event: ShowAnswerDeletedSnackBar) {
        answers.toMutableList().apply {
            if (!isQuestionMultipleChoice && event.answer.isAnswerCorrect) {
                onEachIndexed { index, answer ->
                    set(index, answer.copy(isAnswerCorrect = false))
                }
            }
            add(event.answerIndex, event.answer)
            setAnswerList(this)
        }
    }

    fun onAddAnswerButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromAddEditQuestionToAddEditAnswer(null))
    }

    fun onAnswerItemDragged(from: Int, to: Int) {
        answers.toMutableList().apply {
            add(to, removeAt(from))
            setAnswerList(this)
        }
    }

    fun onAnswerItemSwiped(pos: Int) {
        deleteAnswerFromList(answers[pos])
    }

    fun onChangeQuestionTypeClicked() {
        state.set(QUESTION_TYPE_KEY, !isQuestionMultipleChoice)
        isQuestionMultipleChoiceMutableStateFlow.value = !isQuestionMultipleChoice
    }


    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onSaveButtonClicked() = launch(IO) {
        if (questionText.isEmpty()) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorQuestionHasNoText))
            return@launch
        }

        if (answers.isEmpty()) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorQuestionHasNoAnswers))
            return@launch
        }

        if (answers.any(Answer::answerText / String::isEmpty)) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorAnswersDoNotHaveText))
            return@launch
        }

        if (answers.none(Answer::isAnswerCorrect)) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorSelectOneCorrectAnswer))
            return@launch
        }

        if(!isQuestionMultipleChoice && answers.count(Answer::isAnswerCorrect) > 1) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorQuestionIsSingleChoiceAndHasMultipleCorrectAnswersAddEdit))
            return@launch
        }

        val updatedQuestionWithAnswers = QuestionWithAnswers(
            parsedQuestion.copy(
                questionText = questionText,
                isMultipleChoice = isQuestionMultipleChoice,
                questionPosition = args.questionPosition
            ),
            answers
        )

        eventChannel.send(SaveQuestionWithAnswersEvent(args.questionPosition, updatedQuestionWithAnswers))
        navigationDispatcher.dispatch(NavigateBack)
    }

    sealed class FragmentAddEditQuestionEvent : UiEventMarker {
        class ShowAnswerDeletedSnackBar(val answer: Answer, val answerIndex: Int) : FragmentAddEditQuestionEvent()
        class ShowMessageSnackBar(val messageRes: Int) : FragmentAddEditQuestionEvent()
        class SaveQuestionWithAnswersEvent(val questionPosition: Int, val questionWithAnswers: QuestionWithAnswers) : FragmentAddEditQuestionEvent()
    }

    companion object {
        private const val ANSWERS_KEY = "answersKey"
        private const val QUESTION_TEXT_KEY = "questionTextKey"
        private const val QUESTION_TYPE_KEY = "questionTypeKey"
    }
}