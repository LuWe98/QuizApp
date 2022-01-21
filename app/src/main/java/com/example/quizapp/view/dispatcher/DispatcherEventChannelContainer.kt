package com.example.quizapp.view.dispatcher

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@ActivityRetainedScoped
class DispatcherEventChannelContainer @Inject constructor() {

    private val eventChannel = Channel<DispatchEvent>()

    val eventChannelFlow = eventChannel.receiveAsFlow()

    suspend fun dispatchToQueue(event: DispatchEvent) = eventChannel.send(event)

}