package com.example.quizapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.fragments.addquestionnairescreen.FragmentAddQuestionArgs
import com.example.quizapp.viewmodel.VmAddEditQuestion.FragmentEditQuestionEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmAddEditQuestion @Inject constructor(private val state: SavedStateHandle) : ViewModel() {

    private val args = FragmentAddQuestionArgs.fromSavedStateHandle(state)
    private val questionWithAnswers get() = args.questionWithAnswers
    private val question get() = questionWithAnswers.question
    private val answers get() = questionWithAnswers.answersSortedByPosition


    private val fragmentEditQuestionEventChannel = Channel<FragmentEditQuestionEvent>()

    val fragmentEditQuestionEventChannelFlow get() = fragmentEditQuestionEventChannel.receiveAsFlow()

    var isMultipleChoice = state.get<Boolean>(QUESTION_MULTIPLE_CHOICE) ?: question.isMultipleChoice
        set(value) {
            state.set(QUESTION_MULTIPLE_CHOICE, value)
            field = value
        }

    var questionTitle = state.get<String>(QUESTION_TITLE) ?: question.questionText
        set(value) {
            state.set(QUESTION_TITLE, value)
            field = value
        }

    private val answersMutableLiveData = state.getLiveData(ANSWERS, answers.sortedBy { it.answerPosition }.toMutableList())

    private val answersMutableLiveDataValue get() = answersMutableLiveData.value!!

    val answersLiveData: LiveData<MutableList<Answer>> = answersMutableLiveData.map {
        mutableListOf<Answer>().apply {
            addAll(it.sortedBy {
                it.answerPosition
            })
        }
    }

    private fun setAnswerList(answers: List<Answer>) {
        answersMutableLiveData.value = answers.toMutableList()
        state.set(ANSWERS, answers)
    }

    private fun removeAnswerFromList(answer: Answer) {
        mutableListOf<Answer>().let { lastList ->
            answersMutableLiveDataValue.apply {
                lastList.addAll(this)
                remove(answer)
            }.also {
                setAnswerList(it)
            }

            launch {
                fragmentEditQuestionEventChannel.send(ShowAnswerDeletedSuccessFullySnackBar(lastList))
            }
        }
    }

    fun onAnswerItemClicked(answers: List<Answer>) {
        setAnswerList(answers)
    }

    fun onAnswerItemTextChanged(position: Int, newText: String) {
        answersMutableLiveDataValue.apply {
            set(position, answersMutableLiveDataValue[position].copy(answerText = newText))
        }.also {
            setAnswerList(it)
        }
    }

    fun onAnswerItemDeleteButtonClicked(answer: Answer) {
        removeAnswerFromList(answer)
    }

    fun onAnswerItemSwiped(position: Int) {
        removeAnswerFromList(answersMutableLiveDataValue[position])
    }

    fun onAnswerItemDragReleased(answers: List<Answer>) {
        answers.mapIndexed { index, answer ->
            answer.copy(answerPosition = index)
        }.also {
            setAnswerList(it)
        }
    }

    fun onAddAnswerButtonClicked() {
        answersMutableLiveDataValue.apply {
            add(Answer(answerPosition = (maxOfOrNull { it.answerPosition } ?: -1) + 1))
        }.also {
            setAnswerList(it)
        }
    }

    fun onQuestionEditTextChanged(newText: CharSequence?) {
        questionTitle = newText.toString()
    }

    fun onFabConfirmClicked() {
        if (answersMutableLiveDataValue.none { it.isAnswerCorrect }) {
            launch {
                fragmentEditQuestionEventChannel.send(ShowSelectAtLeastOneCorrectAnswerToast)
            }
            return
        }

        if (answersMutableLiveDataValue.any { it.answerText.isEmpty() }) {
            launch {
                fragmentEditQuestionEventChannel.send(ShowSomeAnswersAreEmptyToast)
            }
            return
        }

        val updatedQuestion = question.copy(questionText = questionTitle, isMultipleChoice = isMultipleChoice)
        val updatedAnswers = answersMutableLiveDataValue.apply { mapIndexed { index, answer -> answer.copy(answerPosition = index) } }
        val updatedQuestionWithAnswers = questionWithAnswers.copy(question = updatedQuestion, answers = updatedAnswers)
        launch {
            fragmentEditQuestionEventChannel.send(SendUpdateRequestToVmAdd(args.questionPosition, updatedQuestionWithAnswers))
        }
    }

    fun onUndoDeleteAnswerClicked(event: ShowAnswerDeletedSuccessFullySnackBar) {
        setAnswerList(event.lastAnswerValues)
    }

    fun onSwitchChanged(checked : Boolean){
        if(isMultipleChoice == checked) return
        isMultipleChoice = checked
        answersMutableLiveDataValue.map {
            it.copy(isAnswerCorrect = false)
        }.also {
            setAnswerList(it)
        }
    }

    sealed class FragmentEditQuestionEvent {
        object ShowSelectAtLeastOneCorrectAnswerToast : FragmentEditQuestionEvent()
        object ShowSomeAnswersAreEmptyToast : FragmentEditQuestionEvent()
        data class ShowAnswerDeletedSuccessFullySnackBar(val lastAnswerValues: List<Answer>) : FragmentEditQuestionEvent()
        data class SendUpdateRequestToVmAdd(val position: Int, val newQuestionWithAnswers: QuestionWithAnswers) : FragmentEditQuestionEvent()
    }

    companion object {
        const val QUESTION_TITLE = "questionTitleKey"
        const val QUESTION_MULTIPLE_CHOICE = "questionMultipleChoiceKey"
        const val ANSWERS = "answersKey"
    }
}