package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.sync.LocallyFilledQuestionnaireToUpload
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.datastore.QuestionnaireShuffleType
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestionsContainerArgs
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizContainerEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmQuizQuestionsContainer @Inject constructor(
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = FragmentQuizQuestionsContainerArgs.fromSavedStateHandle(state)

    val isShowSolutionScreen get() = args.isShowSolutionScreen

    private val fragmentEventChannel = Channel<FragmentQuizContainerEvent>()

    val fragmentEventChannelFlow get() = fragmentEventChannel.receiveAsFlow()

    private var _lastAdapterPosition = state.get<Int>(LAST_ADAPTER_POSITION_KEY) ?: args.questionPosition
        set(value) {
            state.set(LAST_ADAPTER_POSITION_KEY, value)
            field = value
        }

    val lastAdapterPosition get() = _lastAdapterPosition

    fun onMoreOptionsClicked() {
        launch(IO) {
            fragmentEventChannel.send(ShowMoreOptionsPopUpMenuEvent)
        }
    }

    fun onViewPagerPageSelected(position: Int) {
        _lastAdapterPosition = position
    }

    fun onSubmitButtonClicked(areAllQuestionsAnswered: Boolean?) {
        if (areAllQuestionsAnswered == true) {
            launch(IO) {
                fragmentEventChannel.send(OnSubmitButtonClickedEvent)
            }
        }
    }

    fun onQuestionTypeInfoButtonClicked() {
        launch(IO)  {
            fragmentEventChannel.send(ShowQuestionTypeInfoSnackBarEvent)
        }
    }

    fun onMenuItemOrderSelected(shuffleType: QuestionnaireShuffleType) = launch {
        fragmentEventChannel.send(MenuItemOrderSelectedEvent(shuffleType))
    }

    fun onMenuItemClearGivenAnswersClicked(completeQuestionnaire: CompleteQuestionnaire?) = launch(IO) {
        completeQuestionnaire?.apply {
            localRepository.insert(LocallyFilledQuestionnaireToUpload(questionnaire.id))
            allAnswers.map { it.copy(isAnswerSelected = false) }.let {
                localRepository.update(it)
            }
            fragmentEventChannel.send(ShowUndoDeleteGivenAnswersSnackBack(allAnswers))
        }
    }

    fun onUndoDeleteGivenAnswersClick(event: ShowUndoDeleteGivenAnswersSnackBack) = launch(IO) {
        localRepository.update(event.lastAnswerValues)
    }

    sealed class FragmentQuizContainerEvent {
        data class SelectDifferentPage(val newPosition: Int) : FragmentQuizContainerEvent()
        class ShowUndoDeleteGivenAnswersSnackBack(val lastAnswerValues: List<Answer>) : FragmentQuizContainerEvent()
        object ShowMoreOptionsPopUpMenuEvent : FragmentQuizContainerEvent()
        object OnSubmitButtonClickedEvent : FragmentQuizContainerEvent()
        class MenuItemOrderSelectedEvent(val shuffleType: QuestionnaireShuffleType) : FragmentQuizContainerEvent()
        object ShowQuestionTypeInfoSnackBarEvent: FragmentQuizContainerEvent()
    }

    companion object {
        private const val LAST_ADAPTER_POSITION_KEY = "currentVpaPosition"
    }
}