package com.example.quizapp.extensions.flowext

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

inline fun <reified T> Flow<T>.collectLatest(scope: CoroutineScope, crossinline action: (T) -> Unit) {
    scope.launch {
        collectLatest {
            action.invoke(it)
        }
    }
}

inline fun <reified T> Flow<T>.collect(scope: CoroutineScope, crossinline action: (T) -> Unit) {
    scope.launch {
        collect {
            action.invoke(it)
        }
    }
}

/**
 * Livecycle aware Flow-Collection
 */
inline fun <reified T> Flow<T>.awareCollect(
    lifecycleOwner: LifecycleOwner,
    lifeCycleState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline collector: suspend (T) -> Unit = {}
) = apply {
    AwareFlowCollector(lifecycleOwner, this, collector, lifeCycleState)
}

inline fun <reified T> LifecycleOwner.collectWhenStarted(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    crossinline action: suspend (value: T) -> Unit
) {
    collectWhen(flow, firstTimeDelay, Lifecycle.State.STARTED, action)
}

inline fun <reified T> Flow<T>.collectWhenStarted(
    lifecycleOwner: LifecycleOwner,
    firstTimeDelay: Long = 0L,
    crossinline action: suspend (value: T) -> Unit
) {
    lifecycleOwner.collectWhen(this, firstTimeDelay, Lifecycle.State.STARTED, action)
}

inline fun <reified T> LifecycleOwner.collectWhenResumed(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    crossinline action: suspend (value: T) -> Unit
) {
    collectWhen(flow, firstTimeDelay, Lifecycle.State.RESUMED, action)
}

inline fun <reified T> LifecycleOwner.collectWhenCreated(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    crossinline action: suspend (value: T) -> Unit
) {
    collectWhen(flow, firstTimeDelay, Lifecycle.State.CREATED, action)
}

inline fun <reified T> LifecycleOwner.collectWhen(
    flow: Flow<T>,
    firstTimeDelay: Long = 0L,
    onLifeCycleState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend (value: T) -> Unit
) {
    lifecycleScope.launch {
        delay(firstTimeDelay)
        lifecycle.repeatOnLifecycle(onLifeCycleState) {
            flow.collect(action)
        }
    }
}

//fun <T> SavedStateHandle.getStateFlow(key: String, coroutineScope: CoroutineScope, initialValue: T) =
//    getLiveData<T>(key).asFlow().stateIn(coroutineScope, SharingStarted.Lazily, initialValue)


inline fun <reified T> SavedStateHandle.getMutableStateFlow(key: String, initialValue: T? = null) = MutableStateFlow(get<T>(key) ?: initialValue)

inline fun <reified T> SavedStateHandle.getStateFlow(key: String, initialValue: T) = getMutableStateFlow(key, initialValue).asStateFlow()
