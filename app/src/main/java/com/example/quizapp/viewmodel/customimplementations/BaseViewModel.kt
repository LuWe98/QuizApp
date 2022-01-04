package com.example.quizapp.viewmodel.customimplementations

import androidx.lifecycle.ViewModel
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

abstract class BaseViewModel<T : UiEventMarker> : ViewModel() {

    @Inject
    protected lateinit var navigationDispatcher: NavigationDispatcher

    @Inject
    protected lateinit var fragmentResultDispatcher: FragmentResultDispatcher

    protected val eventChannel = Channel<T>()

    val eventChannelFlow = eventChannel.receiveAsFlow()

    sealed class EmptyEventClass: UiEventMarker

}