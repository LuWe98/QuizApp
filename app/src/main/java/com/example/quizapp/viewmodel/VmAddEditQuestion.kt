package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.R
import com.example.quizapp.extensions.div
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.indexOfFirstOrNull
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.fragments.addeditquestionnairescreen.FragmentAddEditQuestionArgs
import com.example.quizapp.viewmodel.VmAddEditQuestion.FragmentAddEditQuestionEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.bson.types.ObjectId
import javax.inject.Inject

@HiltViewModel
class VmAddEditQuestion @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = FragmentAddEditQuestionArgs.fromSavedStateHandle(state)

    private val parsedQuestion
        get() = args.questionWithAnswers?.question
            ?: Question(
                id= ObjectId().toString(),
                questionnaireId = "",
                questionText = "",
                isMultipleChoice = true,
                questionPosition = 0
            )

    private val parsedAnswers get() = args.questionWithAnswers?.answers?.sortedBy(Answer::answerPosition) ?: listOf()


    private val fragmentAddEditQuestionEventChannel = Channel<FragmentAddEditQuestionEvent>()

    val fragmentAddEditQuestionEventChannelFlow = fragmentAddEditQuestionEventChannel.receiveAsFlow()

    private var _questionText = state.get<String>(QUESTION_TEXT_KEY) ?: parsedQuestion.questionText
        set(value) {
            state.set(QUESTION_TEXT_KEY, value)
            field = value
        }

    val questionText get() = _questionText


    private var isQuestionMultipleChoiceMutableStateFlow = state.getMutableStateFlow(QUESTION_TYPE_KEY, parsedQuestion.isMultipleChoice)

    val isQuestionMultipleChoiceStateFlow get() = isQuestionMultipleChoiceMutableStateFlow.asStateFlow()

    val isQuestionMultipleChoice get() = isQuestionMultipleChoiceStateFlow.value

    private var answersMutableStateFlow = state.getMutableStateFlow(ANSWERS_KEY, parsedAnswers)

    val answersStateFlow = answersMutableStateFlow.map { it.sortedBy(Answer::answerPosition) }

    val answers get() = answersMutableStateFlow.value

    private var answerIdToUpdateTextFor = state.get<String>(ANSWER_ID_TO_UPDATE_TEXT_KEY)
        set(value) {
            state.set(ANSWER_ID_TO_UPDATE_TEXT_KEY, value)
            field = value
        }

    private fun findAnswerIndex(answerId: String) = answers.indexOfFirstOrNull { it.id == answerId }

    private fun setAnswerList(answers: List<Answer>) {
        answers.mapIndexed { index, answer -> answer.copy(answerPosition = index) }.let { updatedAnswers ->
            state.set(ANSWERS_KEY, updatedAnswers)
            answersMutableStateFlow.value = updatedAnswers
        }
    }


    fun onQuestionTextChanged(newText: String) {
        _questionText = newText
    }

    fun onAnswerTextUpdateResultReceived(newText: String) {
        findAnswerIndex(answerIdToUpdateTextFor ?: "")?.let { index ->
            answers.toMutableList().apply {
                set(index, get(index).copy(answerText = newText))
                setAnswerList(this)
            }
        }
        answerIdToUpdateTextFor = null
    }

    fun onAnswerClicked(answer: Answer) {
        launch {
            answerIdToUpdateTextFor = answer.id
            fragmentAddEditQuestionEventChannel.send(NavigateToStringSelectDialog(answer.answerText))
        }
    }

    fun onAnswerCheckClicked(answer: Answer) {
        answers.toMutableList().apply {
            if (!isQuestionMultipleChoice) {
                onEachIndexed { index, answer ->
                    set(index, answer.copy(isAnswerCorrect = false))
                }
            }
            set(indexOf(answer), answer.copy(isAnswerCorrect = !answer.isAnswerCorrect))
            setAnswerList(this)
        }
    }


    fun onAnswerDeleteClicked(answer: Answer) {
        answers.toMutableList().apply {
            val answerIndex = indexOf(answer)
            removeAt(answerIndex)
            setAnswerList(this)
            launch {
                fragmentAddEditQuestionEventChannel.send(ShowAnswerDeletedSnackBar(answer, answerIndex))
            }
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

    fun onAddAnswerButtonClicked() {
        answers.toMutableList().apply {
            add(Answer())
            setAnswerList(this)
        }
    }

    fun onChangeQuestionTypeClicked() {
        state.set(QUESTION_TYPE_KEY, !isQuestionMultipleChoice)
        isQuestionMultipleChoiceMutableStateFlow.value = !isQuestionMultipleChoice
        setAnswerList(answers.map { it.copy(isAnswerCorrect = false) })
    }


    fun onSaveButtonClicked() {
        launch {
            if (questionText.isEmpty()) {
                fragmentAddEditQuestionEventChannel.send(ShowMessageSnackBar(R.string.errorQuestionHasNoText))
                return@launch
            }

            if(answers.isEmpty()){
                fragmentAddEditQuestionEventChannel.send(ShowMessageSnackBar(R.string.errorQuestionHasNoAnswers))
                return@launch
            }

            if (answers.none(Answer::isAnswerCorrect)) {
                fragmentAddEditQuestionEventChannel.send(ShowMessageSnackBar(R.string.errorSelectOneCorrectAnswer))
                return@launch
            }

            if (answers.any(Answer::answerText / String::isEmpty)) {
                fragmentAddEditQuestionEventChannel.send(ShowMessageSnackBar(R.string.errorAnswersDoNotHaveText))
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

            fragmentAddEditQuestionEventChannel.send(SaveQuestionWithAnswersEvent(args.questionPosition, updatedQuestionWithAnswers))
        }
    }

    sealed class FragmentAddEditQuestionEvent {
        class NavigateToStringSelectDialog(val initialValue: String) : FragmentAddEditQuestionEvent()
        class ShowAnswerDeletedSnackBar(val answer: Answer, val answerIndex: Int) : FragmentAddEditQuestionEvent()
        class ShowMessageSnackBar(val messageRes: Int) : FragmentAddEditQuestionEvent()
        class SaveQuestionWithAnswersEvent(val questionPosition: Int, val questionWithAnswers: QuestionWithAnswers) : FragmentAddEditQuestionEvent()
    }

    companion object {
        private const val ANSWERS_KEY = "answersKey"
        private const val QUESTION_TEXT_KEY = "questionTextKey"
        private const val QUESTION_TYPE_KEY = "questionTypeKey"
        private const val ANSWER_ID_TO_UPDATE_TEXT_KEY = "answerToUpdateTextKey"
    }
}