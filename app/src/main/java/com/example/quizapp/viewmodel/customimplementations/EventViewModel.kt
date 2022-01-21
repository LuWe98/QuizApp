package com.example.quizapp.viewmodel.customimplementations

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class EventViewModel<T : UiEventMarker> : BaseViewModel() {

    protected val eventChannel = Channel<T>()

    val eventChannelFlow = eventChannel.receiveAsFlow()

}