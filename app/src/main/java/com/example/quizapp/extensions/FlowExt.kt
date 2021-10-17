package com.example.quizapp.extensions

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun <T> Flow<T>.collectLatest(scope: CoroutineScope, action: (T) -> Unit) {
    scope.launch {
        collectLatest {
            action.invoke(it)
        }
    }
}

fun <T> Flow<T>.collect(scope: CoroutineScope, action: (T) -> Unit) {
    scope.launch {
        collect {
            action.invoke(it)
        }
    }
}

fun <T> Flow<T>.first(scope: CoroutineScope): T = runBlocking(scope.coroutineContext) {
    first()
}

fun <T> Flow<T>.first(dispatcher: CoroutineDispatcher): T = runBlocking(dispatcher) {
    first()
}

fun <T> Flow<T>.firstOrNull(scope: CoroutineScope): T? = runBlocking(scope.coroutineContext) {
    firstOrNull()
}


inline fun <reified T> Flow<T>.observe(
    lifecycleOwner: LifecycleOwner,
    lifeCycleState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline collector: suspend (T) -> Unit = {}
) = apply {
    FlowLifecycleHelper(lifecycleOwner, this, collector, lifeCycleState)
}


fun <T> LifecycleOwner.collectWhenStarted(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    action: suspend (value: T) -> Unit
) {
    collectWhen(flow, firstTimeDelay, Lifecycle.State.STARTED, action)
}

fun <T> LifecycleOwner.collectWhenResumed(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    action: suspend (value: T) -> Unit
) {
    collectWhen(flow, firstTimeDelay, Lifecycle.State.RESUMED, action)
}

fun <T> LifecycleOwner.collectWhenCreated(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    action: suspend (value: T) -> Unit
) {
    collectWhen(flow, firstTimeDelay, Lifecycle.State.CREATED, action)
}

fun <T> LifecycleOwner.collectWhen(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    onLifeCycleState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (value: T) -> Unit
) {
    lifecycleScope.launch {
        delay(firstTimeDelay)
        lifecycle.repeatOnLifecycle(onLifeCycleState) {
            flow.collect(action)
        }
    }
}