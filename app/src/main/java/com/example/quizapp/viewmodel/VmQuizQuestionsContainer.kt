package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestionsContainerArgs
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizOverviewEvent.*
import com.example.quizapp.view.viewpager.adapter.VpaQuiz
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

    private val fragmentEventChannel = Channel<FragmentQuizOverviewEvent>()

    val fragmentEventChannelFlow get() = fragmentEventChannel.receiveAsFlow()

    var lastAdapterPosition = state.get<Int>(LAST_ADAPTER_POSITION_KEY) ?: args.questionPosition
        set(value) {
            state.set(LAST_ADAPTER_POSITION_KEY, value)
            field = value
        }

    fun onMoreOptionsClicked(){
        launch(IO) {
            fragmentEventChannel.send(ShowMoreOptionsPopUpMenu)
        }
    }

    fun onViewPagerPageSelected(position: Int) {
        lastAdapterPosition = position
    }

    fun onSubmitButtonClicked(areAllQuestionsAnswered: Boolean?) {
        if (areAllQuestionsAnswered == true) {
            launch(IO) {
                fragmentEventChannel.send(OnSubmitButtonClickedEvent)
            }
        }
    }

    sealed class FragmentQuizOverviewEvent {
        data class SelectDifferentPage(val newPosition: Int) : FragmentQuizOverviewEvent()
        object ShowMoreOptionsPopUpMenu: FragmentQuizOverviewEvent()
        object OnSubmitButtonClickedEvent : FragmentQuizOverviewEvent()
    }

    companion object {
        const val LAST_ADAPTER_POSITION_KEY = "currentVpaPosition"
    }
}


//fun onSelectPreviousPageButtonClicked() = launch(IO) {
//    if (lastAdapterPosition != 0) {
//        fragmentEventChannel.send(SelectDifferentPage(lastAdapterPosition - 1))
//    }
//}
//
//fun onSelectNextPageButtonClicked(vpaQuiz: VpaQuiz) = launch(IO) {
//    if (lastAdapterPosition != vpaQuiz.itemCount - 1) {
//        fragmentEventChannel.send(SelectDifferentPage(lastAdapterPosition + 1))
//    }
//}
//    private val questionIdListMutableStateFlow = state.getMutableStateFlow<MutableList<String>>(QUESTION_ID_LIST_KEY, mutableListOf())
//
//    private val questionIdList get() = questionIdListMutableStateFlow.value
//
//    fun questionIdStateFlow(questionId: String) = questionIdListMutableStateFlow
//        .map { it.firstOrNull { id -> id == questionId } }
//        .distinctUntilChanged()
//
//
//    private fun addOrRemoveQuestionToDisplaySolution(questionId: String) {
//        if (questionIdList.contains(questionId)) {
//            questionIdList.remove(questionId)
//        } else {
//            questionIdList.add(questionId)
//        }
//        state.set(QUESTION_ID_LIST_KEY, questionIdList)
//    }
//
//    fun shouldDisplayQuestionSolution(questionId: String) = questionIdList.contains(questionId)
//
//
//    fun onShowSolutionButtonClicked(vpaQuiz: VpaQuiz) {
//        vpaQuiz.createFragment(lastAdapterPosition).questionId.let {
//            addOrRemoveQuestionToDisplaySolution(it)
//            launch(IO) {
//                fragmentEventChannel.send(ChangeSolutionButtonTint(shouldDisplayQuestionSolution(it)))
//            }
//        }
//    }