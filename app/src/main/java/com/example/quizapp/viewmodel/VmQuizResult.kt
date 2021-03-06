package com.example.quizapp.viewmodel

import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.FromQuizResultToQuizContainerScreen
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.NavigateBack
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@HiltViewModel
class VmQuizResult @Inject constructor(
    private val localRepository: LocalRepository
) : BaseViewModel() {

    fun onShowSolutionsClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromQuizResultToQuizContainerScreen( true))
    }

    fun onCloseButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onTryAgainClicked(completeQuestionnaire: CompleteQuestionnaire?) = launch(IO) {
        completeQuestionnaire?.apply {
            localRepository.update(allAnswers.map { it.copy(isAnswerSelected = false) })
        }
        navigationDispatcher.dispatch(FromQuizResultToQuizContainerScreen(false))
    }

    sealed class FragmentQuizResultEvent: UiEventMarker
}