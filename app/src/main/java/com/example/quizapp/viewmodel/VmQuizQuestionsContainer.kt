package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestionsContainerArgs
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizOverviewEvent.*
import com.example.quizapp.view.viewpager.adapter.VpaQuiz
import dagger.hilt.android.lifecycle.HiltViewModel
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

    val fragmentEventChannelFlow get() = fragmentEventChannel.receiveAsFlow()

    var lastAdapterPosition = state.get<Int>(LAST_ADAPTER_POSITION_KEY) ?: args.questionPosition
        set(value) {
            state.set(LAST_ADAPTER_POSITION_KEY, value)
            field = value
        }

    private val questionIdListLiveData = state.getLiveData<MutableList<String>>(QUESTION_ID_LIST_KEY, mutableListOf())

    private val questionIdList get() = questionIdListLiveData.value!!

    fun questionIdLiveData(questionId: String) = questionIdListLiveData.map { it.firstOrNull { id -> id == questionId } }.distinctUntilChanged()

    private fun addOrRemoveQuestionToDisplaySolution(questionId : String){
        if(questionIdList.contains(questionId)){
            questionIdList.remove(questionId)
        } else {
            questionIdList.add(questionId)
        }
        state.set(QUESTION_ID_LIST_KEY, questionIdList)
    }

    fun shouldDisplayQuestionSolution(questionId: String) = questionIdList.contains(questionId)


    fun onViewPagerPageSelected(position : Int){
        lastAdapterPosition = position
    }

    fun onSelectPreviousPageButtonClicked(){
        if(lastAdapterPosition != 0){
            launch {
                fragmentEventChannel.send(SelectDifferentPage(lastAdapterPosition - 1))
            }
        }
    }

    fun onSelectNextPageButtonClicked(vpaQuiz: VpaQuiz){
        if(lastAdapterPosition != vpaQuiz.itemCount -1){
            launch {
                fragmentEventChannel.send(SelectDifferentPage(lastAdapterPosition + 1))
            }
        }
    }

    fun onCheckResultsButtonClicked(){
        launch {
            fragmentEventChannel.send(CheckResultsEvent)
        }
    }

    fun onShowSolutionButtonClicked(vpaQuiz: VpaQuiz){
        launch {
            vpaQuiz.getFragment(lastAdapterPosition).questionId.let {
                addOrRemoveQuestionToDisplaySolution(it)
                fragmentEventChannel.send(ChangeSolutionButtonTint(shouldDisplayQuestionSolution(it)))
            }
        }
    }

    fun onAnswerItemClicked(list: List<Answer>){
        launch { localRepository.update(list) }
    }



    sealed class FragmentQuizOverviewEvent {
        object CheckResultsEvent : FragmentQuizOverviewEvent()
        data class SelectDifferentPage(val newPosition : Int) : FragmentQuizOverviewEvent()
        data class ChangeSolutionButtonTint(val show : Boolean) : FragmentQuizOverviewEvent()
    }

    companion object {
        const val LAST_ADAPTER_POSITION_KEY = "currentVpaPosition"
        const val QUESTION_ID_LIST_KEY = "questionIdListKey"
    }
}