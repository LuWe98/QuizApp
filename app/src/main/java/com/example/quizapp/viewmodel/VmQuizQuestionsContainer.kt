package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.LocallyFilledQuestionnaireToUpload
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestionsContainerArgs
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.*
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizContainerEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@HiltViewModel
class VmQuizQuestionsContainer @Inject constructor(
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : BaseViewModel<FragmentQuizContainerEvent>() {

    private val args = FragmentQuizQuestionsContainerArgs.fromSavedStateHandle(state)

    val isShowSolutionScreen get() = args.isShowSolutionScreen

    private var _lastAdapterPosition = state.get<Int>(LAST_ADAPTER_POSITION_KEY) ?: args.questionPosition
        set(value) {
            state.set(LAST_ADAPTER_POSITION_KEY, value)
            field = value
        }

    val lastAdapterPosition get() = _lastAdapterPosition

    fun onMoreOptionsClicked() = launch(IO) {
        eventChannel.send(ShowMoreOptionsPopUpMenuEvent)
    }

    fun onViewPagerPageSelected(position: Int) {
        _lastAdapterPosition = position
    }

    fun onSubmitButtonClicked(areAllQuestionsAnswered: Boolean?) = launch(IO) {
        if (areAllQuestionsAnswered == true) {
            navigationDispatcher.dispatch(FromQuizContainerToQuizResultScreen)
        }
    }

    fun onQuestionTypeInfoButtonClicked() = launch(IO) {
        eventChannel.send(ShowQuestionTypeInfoSnackBarEvent)
    }

    fun onMenuItemOrderSelected(shuffleType: QuestionnaireShuffleType) = launch(IO) {
        if (preferencesRepository.getShuffleType() != shuffleType) {
            preferencesRepository.updateShuffleSeed()
            preferencesRepository.updateShuffleType(shuffleType)
            eventChannel.send(ShowMessageSnackBarEvent(R.string.shuffleTypeChanged))
            eventChannel.send(ResetViewPagerEvent)
        }
    }

    fun onShuffleButtonClicked() = launch(IO) {
        preferencesRepository.updateShuffleSeed()
        eventChannel.send(ResetViewPagerEvent)
    }

    fun onMenuItemClearGivenAnswersClicked(completeQuestionnaire: CompleteQuestionnaire?) = launch(IO) {
        completeQuestionnaire?.apply {
            localRepository.insert(LocallyFilledQuestionnaireToUpload(questionnaire.id))
            allAnswers.map { it.copy(isAnswerSelected = false) }.let {
                localRepository.update(it)
            }
            eventChannel.send(ShowUndoDeleteGivenAnswersSnackBack(allAnswers))
        }
    }

    fun onUndoDeleteGivenAnswersClick(event: ShowUndoDeleteGivenAnswersSnackBack) = launch(IO) {
        localRepository.update(event.lastAnswerValues)
    }

    sealed class FragmentQuizContainerEvent: ViewModelEventMarker {
        class SelectDifferentPage(val newPosition: Int) : FragmentQuizContainerEvent()
        class ShowUndoDeleteGivenAnswersSnackBack(val lastAnswerValues: List<Answer>) : FragmentQuizContainerEvent()
        class ShowMessageSnackBarEvent(@StringRes val messageRes: Int) : FragmentQuizContainerEvent()
        object ShowMoreOptionsPopUpMenuEvent : FragmentQuizContainerEvent()
        object ShowQuestionTypeInfoSnackBarEvent : FragmentQuizContainerEvent()
        object ResetViewPagerEvent : FragmentQuizContainerEvent()
    }

    companion object {
        private const val LAST_ADAPTER_POSITION_KEY = "currentVpaPosition"
    }
}