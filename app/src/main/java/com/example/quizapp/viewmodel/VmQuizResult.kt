package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.viewmodel.VmQuizResult.FragmentQuizResultEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmQuizResult @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    private val fragmentQuizResultEventChannel = Channel<FragmentQuizResultEvent>()

    val fragmentQuizResultEventChannelFlow = fragmentQuizResultEventChannel.receiveAsFlow()

    private var _retryQuiz: Boolean = false

    val retryQuiz get() = _retryQuiz

    fun onShowSolutionsClicked() {
        launch(IO) {
            fragmentQuizResultEventChannel.send(ShowSolutionsEvent)
        }
    }

    fun onCloseButtonClicked() {
        launch(IO) {
            fragmentQuizResultEventChannel.send(NavigateBackEvent)
        }
    }

    fun onTryAgainClicked(completeQuestionnaire: CompleteQuestionnaire?) {
        launch(IO) {
            completeQuestionnaire?.apply {
                localRepository.update(allAnswers.map { it.copy(isAnswerSelected = false) })
            }
            _retryQuiz = true
        }
    }

    sealed class FragmentQuizResultEvent {
        object NavigateBackEvent : FragmentQuizResultEvent()
        object ShowSolutionsEvent : FragmentQuizResultEvent()
    }
}