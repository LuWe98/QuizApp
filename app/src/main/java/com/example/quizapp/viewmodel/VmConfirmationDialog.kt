package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.extensions.launch
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.NavigateBack
import com.example.quizapp.view.fragments.dialogs.confirmation.DfConfirmationArgs
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@HiltViewModel
class VmConfirmationDialog @Inject constructor(
    state: SavedStateHandle
) : BaseViewModel<BaseViewModel.EmptyEventClass>(){

    private val args = DfConfirmationArgs.fromSavedStateHandle(state)

    val confirmationType get() = args.confirmationType

    fun onConfirmButtonClicked() = launch(IO) {
        confirmationType.responseProvider(true).let { result ->
             fragmentResultDispatcher.dispatch(result)
        }
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onCancelButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }
}