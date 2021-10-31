package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import com.example.quizapp.QuizNavGraphArgs
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.InsertFilledQuestionnaireResponse.*
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.questionnaire.Answer
import com.example.quizapp.model.room.entities.sync.LocallyAnsweredQuestionnaire
import com.example.quizapp.viewmodel.VmQuiz.FragmentQuizEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VmQuiz @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = QuizNavGraphArgs.fromSavedStateHandle(state)

    private val fragmentEventChannel = Channel<FragmentQuizEvent>()

    val fragmentEventChannelFlow get() = fragmentEventChannel.receiveAsFlow()

    private val completeQuestionnaireFlow = localRepository.findCompleteQuestionnaireAsFlowWith(args.questionnaireId)

    val completeQuestionnaireLiveData = completeQuestionnaireFlow.asLiveData().distinctUntilChanged()

    val completeQuestionnaire get() = completeQuestionnaireLiveData.value

    fun getQuestionWithAnswersLiveData(questionId: String) = completeQuestionnaireLiveData.map { it.getQuestionWithAnswers(questionId) }.distinctUntilChanged()

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

    fun onShowSolutionClick() {
        completeQuestionnaire?.let {
            if (it.areAllQuestionsAnswered) {
                setShouldDisplaySolution(!shouldDisplaySolution)
            } else {
                launch(IO) { fragmentEventChannel.send(ShowCompleteAllAnswersToast) }
            }
        }
    }

    fun onMoreOptionsItemClicked() {
        launch(IO) { fragmentEventChannel.send(ShowPopupMenu) }
    }

    fun onUndoDeleteGivenAnswersClick(event: ShowUndoDeleteGivenAnswersSnackBack) {
        launch(IO) { localRepository.update(event.lastAnswerValues) }
    }

    fun onClearGivenAnswersClicked() {
        launch(IO) {
            completeQuestionnaire?.apply {
                mutableListOf<Answer>().let { list ->
                    list.addAll(allAnswers)
                    fragmentEventChannel.send(ShowUndoDeleteGivenAnswersSnackBack(list))
                    list.map {
                        it.copy(isAnswerSelected = false)
                    }.also {
                        localRepository.update(it)
                    }
                }
            }
        }
    }

    fun onFabClicked() {
        if (shouldDisplaySolution) {
            setShouldDisplaySolution(false)
        }
        launch(IO) {
            fragmentEventChannel.send(NavigateToQuizScreen)
        }
    }



    fun onAnswerItemClicked(selectedAnswerId: String, allAnswers: List<Answer>, isQuestionMultipleChoice: Boolean) = launch(IO) {
        if (isQuestionMultipleChoice) {
            allAnswers.firstOrNull { it.id == selectedAnswerId }?.let { answer ->
                localRepository.update(answer.copy(isAnswerSelected = !answer.isAnswerSelected))
            }
        } else {
            localRepository.update(allAnswers.map { it.copy(isAnswerSelected = it.id == selectedAnswerId) })
        }

        completeQuestionnaire?.let {
            localRepository.insert(LocallyAnsweredQuestionnaire(it.questionnaire.id))
        }
    }


    override fun onCleared() {
        super.onCleared()
        uploadGivenAnswers()
    }

    private fun uploadGivenAnswers() = applicationScope.launch(IO) {
        completeQuestionnaire?.let {
            if(it.questionnaire.syncStatus != SyncStatus.SYNCED) {
                return@launch
            }

            val filledQuestionnaire = DataMapper.mapSqlEntitiesToFilledMongoEntity(it)

            if(localRepository.isAnsweredQuestionnairePresent(filledQuestionnaire.questionnaireId)){
                runCatching {
                    backendRepository.insertFilledQuestionnaire(filledQuestionnaire)
                }.onSuccess { response ->
                    if(response.responseType != InsertFilledQuestionnaireResponseType.ERROR){
                        localRepository.delete(LocallyAnsweredQuestionnaire(filledQuestionnaire.questionnaireId))
                    }
                }
            }
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
}