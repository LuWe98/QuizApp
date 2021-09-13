package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.example.quizapp.ui.fragments.quizscreen.FragmentQuizQuestionsContainerArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VmQuizQuestionsContainer @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = FragmentQuizQuestionsContainerArgs.fromSavedStateHandle(state)

    var lastAdapterPosition = state.get<Int>(LAST_ADAPTER_POSITION_KEY) ?: args.questionPosition
        set(value) {
            state.set(LAST_ADAPTER_POSITION_KEY, value)
            field = value
        }

    private val questionIdListLiveData = state.getLiveData<MutableList<Long>>(QUESTION_ID_LIST_KEY, mutableListOf())

    fun questionIdLiveData(questionId: Long) = questionIdListLiveData.map {
        it.firstOrNull { id -> id == questionId }
    }.distinctUntilChanged()

    private val questionIdList get() = questionIdListLiveData.value!!

    fun addOrRemoveQuestionToDisplaySolution(questionId : Long){
        if(questionIdList.contains(questionId)){
            questionIdList.remove(questionId)
        } else {
            questionIdList.add(questionId)
        }
        state.set(QUESTION_ID_LIST_KEY, questionIdList)
    }

    fun isQuestionIdInsideShouldDisplayList(questionId: Long) = questionIdList.contains(questionId)

    companion object {
        const val LAST_ADAPTER_POSITION_KEY = "currentVpaPosition"
        const val QUESTION_ID_LIST_KEY = "questionIdListKey"
    }
}