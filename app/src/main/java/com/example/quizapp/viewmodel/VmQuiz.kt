package com.example.quizapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.quizapp.QuizNavGraphArgs
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.EntityMarker
import com.example.quizapp.viewmodel.VmQuiz.FragmentQuizEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmQuiz @Inject constructor(
    application: Application,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(application) {

    init {
        log("STATE ${state.get<String>(SHOULD_DISPLAY_SOLUTION)}")
    }

    private val args = QuizNavGraphArgs.fromSavedStateHandle(state)

    private val fragmentEventChannel = Channel<FragmentQuizEvent>()

    val fragmentEventChannelFlow get() = fragmentEventChannel.receiveAsFlow()

    val completeQuestionnaireLiveData = localRepository.getCompleteQuestionnaireWithIdLiveData(args.questionnaireId).distinctUntilChanged()

    fun getQuestionWithAnswersLiveData(questionId: Long) = completeQuestionnaireLiveData.map { it.getQuestionWithAnswers(questionId) }.distinctUntilChanged()

    val completeQuestionnaire get() = completeQuestionnaireLiveData.value

    val questionnaireLiveData get() = completeQuestionnaireLiveData.map { it.questionnaire }.distinctUntilChanged()

    val questionsWithAnswersLiveData get() = completeQuestionnaireLiveData.map { it.questionsWithAnswers }.distinctUntilChanged()

    val answeredQuestionsPercentageLiveData get() = completeQuestionnaireLiveData.map { it.answeredQuestionsPercentage }.distinctUntilChanged()

    val allQuestionsAnsweredLiveData get() = answeredQuestionsPercentageLiveData.map { it == 100 }.distinctUntilChanged()

    val shouldDisplaySolutionLiveData get() = state.getLiveData(SHOULD_DISPLAY_SOLUTION, false)

    val shouldDisplaySolution get() = shouldDisplaySolutionLiveData.value!!


    fun setShouldDisplaySolution(shouldDisplaySolution: Boolean) {
        state.set(SHOULD_DISPLAY_SOLUTION, shouldDisplaySolution)
        shouldDisplaySolutionLiveData.value = shouldDisplaySolution
    }

    private fun update(entity: List<EntityMarker>) {
        launch { localRepository.update(entity) }
    }

    fun onShowSolutionClick() {
        completeQuestionnaire?.let {
            if (it.areAllQuestionsAnswered) {
                setShouldDisplaySolution(!shouldDisplaySolution)
            } else {
                launch { fragmentEventChannel.send(ShowCompleteAllAnswersToast) }
            }
        }
    }

    fun onMoreOptionsItemClicked() {
        launch { fragmentEventChannel.send(ShowPopupMenu) }
    }

    fun onUndoDeleteGivenAnswersClick(event: ShowUndoDeleteGivenAnswersSnackBack) {
        update(event.lastAnswerValues)
    }

    fun onClearGivenAnswersClicked(){
        completeQuestionnaire?.apply {
            mutableListOf<Answer>().let { list ->
                questionsWithAnswers.forEach { qwa -> list.addAll(qwa.answers) }
                launch {
                    fragmentEventChannel.send(ShowUndoDeleteGivenAnswersSnackBack(list))
                    list.map { it.copy(isAnswerSelected = false) }.also { update(it) }
                }
            }
        }
    }

    fun onFabClicked(){
        if(shouldDisplaySolution){
            setShouldDisplaySolution(false)
        }
        launch {
            fragmentEventChannel.send(NavigateToQuizScreen)
        }
    }


    sealed class FragmentQuizEvent {
        object ShowCompleteAllAnswersToast : FragmentQuizEvent()
        data class ShowUndoDeleteGivenAnswersSnackBack(val lastAnswerValues: List<Answer>) : FragmentQuizEvent()
        object ShowPopupMenu : FragmentQuizEvent()
        object NavigateToQuizScreen : FragmentQuizEvent()
    }

    companion object {
        const val SHOULD_DISPLAY_SOLUTION = "shouldDisplaySolutionKey"
    }



//    private val completeQuestionnaireStateFlow = localRepository.completeQuestionnaireStateFlow(args.questionnaireId)
//
//    val completeQuestionnaireStateFlowOpen = localRepository.completeQuestionnaireStateFlow(args.questionnaireId).filterNotNull().distinctUntilChanged()
//
//    fun getQuestionWithAnswersFlow(questionId: Long) = completeQuestionnaireStateFlow.mapNotNull { it?.getQuestionWithAnswers(questionId) }.distinctUntilChanged()
//
//    val completeQuestionnaireFlowValue get() = completeQuestionnaireStateFlow.value
//
//    val questionnaireFlow get() = completeQuestionnaireStateFlow.mapNotNull { it?.questionnaire }.distinctUntilChanged()
//
//    val questionsWithAnswersFlow get() = completeQuestionnaireStateFlow.mapNotNull { it?.questionsWithAnswers }.distinctUntilChanged()
//
//    val answeredQuestionsPercentageFlow get() = completeQuestionnaireStateFlow.mapNotNull { it?.answeredQuestionsPercentage }.distinctUntilChanged()
//
//    val allQuestionsAnsweredFlow get() = answeredQuestionsPercentageFlow.map { it == 100 }.distinctUntilChanged()
//
//    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


}