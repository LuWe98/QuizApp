package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestionsContainerArgs
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizOverviewEvent.*
import com.example.quizapp.view.viewpager.adapter.VpaQuiz
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmQuizQuestionsContainer @Inject constructor(
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = FragmentQuizQuestionsContainerArgs.fromSavedStateHandle(state)

    private val fragmentEventChannel = Channel<FragmentQuizOverviewEvent>()

    val fragmentEventChannelLD get() = fragmentEventChannel.receiveAsFlow().asLiveData()

    var lastAdapterPosition = state.get<Int>(LAST_ADAPTER_POSITION_KEY) ?: args.questionPosition
        set(value) {
            state.set(LAST_ADAPTER_POSITION_KEY, value)
            field = value
        }

    private val questionIdListLiveData = state.getLiveData<MutableList<String>>(QUESTION_ID_LIST_KEY, mutableListOf())

    private val questionIdList get() = questionIdListLiveData.value!!

    fun questionIdLiveData(questionId: String) = questionIdListLiveData.map { it.firstOrNull { id -> id == questionId } }.distinctUntilChanged()

    private fun addOrRemoveQuestionToDisplaySolution(questionId: String) {
        if (questionIdList.contains(questionId)) {
            questionIdList.remove(questionId)
        } else {
            questionIdList.add(questionId)
        }
        state.set(QUESTION_ID_LIST_KEY, questionIdList)
    }

    fun shouldDisplayQuestionSolution(questionId: String) = questionIdList.contains(questionId)


    fun onViewPagerPageSelected(position: Int) {
        lastAdapterPosition = position
    }

    fun onSelectPreviousPageButtonClicked() = launch(IO) {
        if (lastAdapterPosition != 0) {
            fragmentEventChannel.send(SelectDifferentPage(lastAdapterPosition - 1))
        }
    }

    fun onSelectNextPageButtonClicked(vpaQuiz: VpaQuiz) = launch(IO) {
        if (lastAdapterPosition != vpaQuiz.itemCount - 1) {
            fragmentEventChannel.send(SelectDifferentPage(lastAdapterPosition + 1))
        }
    }

    fun onCheckResultsButtonClicked() = launch(IO) {
        fragmentEventChannel.send(CheckResultsEvent)
    }

    fun onShowSolutionButtonClicked(vpaQuiz: VpaQuiz) = launch(IO) {
        vpaQuiz.getFragment(lastAdapterPosition).questionId.let {
            addOrRemoveQuestionToDisplaySolution(it)
            fragmentEventChannel.send(ChangeSolutionButtonTint(shouldDisplayQuestionSolution(it)))
        }

    }


    sealed class FragmentQuizOverviewEvent {
        object CheckResultsEvent : FragmentQuizOverviewEvent()
        data class SelectDifferentPage(val newPosition: Int) : FragmentQuizOverviewEvent()
        data class ChangeSolutionButtonTint(val show: Boolean) : FragmentQuizOverviewEvent()
    }

    companion object {
        const val LAST_ADAPTER_POSITION_KEY = "currentVpaPosition"
        const val QUESTION_ID_LIST_KEY = "questionIdListKey"
    }
}