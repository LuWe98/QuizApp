package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.sync.LocallyFilledQuestionnaireToUpload
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType
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
    private val preferencesRepository: PreferencesRepository,
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

    fun onMenuItemOrderSelected(shuffleType: QuestionnaireShuffleType) {
        launch(IO) {
            if (preferencesRepository.getShuffleType() != shuffleType) {
                preferencesRepository.updateShuffleSeed()
                preferencesRepository.updateShuffleType(shuffleType)
                fragmentEventChannel.send(ShowMessageSnackBarEvent(R.string.shuffleTypeChanged))
                fragmentEventChannel.send(ResetViewPagerEvent)
            }
        }
    }

    fun onShuffleButtonClicked(){
        launch(IO) {
            preferencesRepository.updateShuffleSeed()
            fragmentEventChannel.send(ResetViewPagerEvent)
        }
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
        class SelectDifferentPage(val newPosition: Int) : FragmentQuizContainerEvent()
        class ShowUndoDeleteGivenAnswersSnackBack(val lastAnswerValues: List<Answer>) : FragmentQuizContainerEvent()
        class ShowMessageSnackBarEvent(@StringRes val messageRes: Int): FragmentQuizContainerEvent()
        object ShowMoreOptionsPopUpMenuEvent : FragmentQuizContainerEvent()
        object OnSubmitButtonClickedEvent : FragmentQuizContainerEvent()
        object ShowQuestionTypeInfoSnackBarEvent: FragmentQuizContainerEvent()
        object ResetViewPagerEvent: FragmentQuizContainerEvent()
    }

    companion object {
        private const val LAST_ADAPTER_POSITION_KEY = "currentVpaPosition"
    }
}