package com.example.quizapp.view.dispatcher

import androidx.annotation.CallSuper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class Dispatcher <T : DispatchEvent> {

    private val dispatchEventChannel = Channel<T>()

    val dispatchEventChannelFlow = dispatchEventChannel.receiveAsFlow()

    @CallSuper
    open suspend fun dispatch(event: T) = dispatchEventChannel.send(event)

}