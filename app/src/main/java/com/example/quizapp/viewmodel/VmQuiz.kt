package com.example.quizapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.quizapp.QuizNavGraphArgs
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.EntityMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmQuiz @Inject constructor(
    application: Application,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(application) {

    private val args = QuizNavGraphArgs.fromSavedStateHandle(state)

    private val fragmentEventChannel = Channel<FragmentQuizEvent>()

    val fragmentEventChannelFlow get() = fragmentEventChannel.receiveAsFlow()

    val completeQuestionnaireLiveData = localRepository.getCompleteQuestionnaireWithIdLiveData(args.questionnaireId).distinctUntilChanged()

    val completeQuestionnaire get() = completeQuestionnaireLiveData.value

    val questionnaireLiveData get() = completeQuestionnaireLiveData.map { it.questionnaire }.distinctUntilChanged()

    val questionsWithAnswersLiveData get() = completeQuestionnaireLiveData.map { it.questionsWithAnswers }.distinctUntilChanged()

    fun getQuestionWithAnswersLiveData(questionId: Long) = completeQuestionnaireLiveData.map { it.getQuestionWithAnswers(questionId) }.distinctUntilChanged()

    val answeredQuestionsPercentageLiveData = completeQuestionnaireLiveData.map { it.answeredQuestionsPercentage }.distinctUntilChanged()

    val allQuestionsAnsweredLiveData = answeredQuestionsPercentageLiveData.map { it == 100 }.distinctUntilChanged()

    val shouldDisplaySolutionLiveData = state.getLiveData(SHOULD_DISPLAY_SOLUTION, false)

    val shouldDisplaySolution get() = shouldDisplaySolutionLiveData.value!!

    fun setShouldDisplaySolution(shouldDisplaySolution: Boolean) {
        state.set(SHOULD_DISPLAY_SOLUTION, shouldDisplaySolution)
        shouldDisplaySolutionLiveData.value = shouldDisplaySolution
    }

    fun update(entity: EntityMarker) {
        launch { localRepository.update(entity) }
    }

    fun update(entity: List<EntityMarker>) {
        launch { localRepository.update(entity) }
    }

    fun onMoreOptionsClicked() {
        launch {
            fragmentEventChannel.send(FragmentQuizEvent.ShowPopupMenu)
        }
    }

    fun onClearGivenAnswersClicked(){
        completeQuestionnaire?.apply {
            mutableListOf<Answer>().let { list ->
                questionsWithAnswers.forEach { qwa -> list.addAll(qwa.answers) }
                launch {
                    fragmentEventChannel.send(FragmentQuizEvent.ShowUndoDeleteGivenAnswersSnackBack(list))
                    list.map { it.copy(isAnswerSelected = false) }.also { update(it) }
                }
            }
        }
    }

    fun onCheckResultsClick() {
        completeQuestionnaire?.let {
            if (it.areAllQuestionsAnswered) {
                setShouldDisplaySolution(!shouldDisplaySolution)
            } else {
                launch { fragmentEventChannel.send(FragmentQuizEvent.ShowCompleteAllAnswersToast) }
            }
        }
    }

    fun onUndoDeleteGivenAnswersClick(event: FragmentQuizEvent.ShowUndoDeleteGivenAnswersSnackBack) {
        update(event.lastAnswerValues)
    }

    companion object {
        const val SHOULD_DISPLAY_SOLUTION = "shouldDisplaySolutionKey"
    }

    sealed class FragmentQuizEvent {
        object ShowCompleteAllAnswersToast : FragmentQuizEvent()
        data class ShowUndoDeleteGivenAnswersSnackBack(val lastAnswerValues: List<Answer>) : FragmentQuizEvent()
        object ShowPopupMenu : FragmentQuizEvent()
    }
}