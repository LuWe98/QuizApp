package com.example.quizapp.viewmodel

import com.example.quizapp.extensions.launch
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@HiltViewModel
class VmVoiceSearch @Inject constructor(): BaseViewModel() {

    fun onVoiceSearchDone() = launch(IO) {
        navigationDispatcher.dispatch(NavigationDispatcher.NavigationEvent.NavigateBack)
    }

}