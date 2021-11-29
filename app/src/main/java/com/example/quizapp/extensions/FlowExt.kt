package com.example.quizapp.extensions

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Collects the Flow when the lifecycle of the provided LifecycleOwner reaches 'Lifecycle.State.STARTED'
 */
inline fun <reified T> Flow<T>.collectWhenStarted(
    viewLifecycleOwner: LifecycleOwner,
    firstTimeDelay: Long = 0L,
    crossinline collector: suspend (T) -> Unit = {}
) = collectWhen(viewLifecycleOwner, firstTimeDelay, Lifecycle.State.STARTED, collector)

/**
 * Collects the Flow when the lifecycle of the provided LifecycleOwner reaches 'Lifecycle.State.RESUMED'
 */
inline fun <reified T> Flow<T>.collectWhenResumed(
    viewLifecycleOwner: LifecycleOwner,
    firstTimeDelay: Long = 0L,
    crossinline collector: suspend (T) -> Unit = {}
) = collectWhen(viewLifecycleOwner, firstTimeDelay, Lifecycle.State.RESUMED, collector)

/**
 * Collects the Flow when the lifecycle of the provided LifecycleOwner reaches 'Lifecycle.State.CREATED'
 */
inline fun <reified T> Flow<T>.collectWhenCreated(
    viewLifecycleOwner: LifecycleOwner,
    firstTimeDelay: Long = 0L,
    crossinline collector: suspend (T) -> Unit = {}
) = collectWhen(viewLifecycleOwner, firstTimeDelay, Lifecycle.State.CREATED, collector)


inline fun <reified T> Flow<T>.collectWhen(
    lifecycleOwner: LifecycleOwner,
    firstTimeDelay: Long = 0L,
    onLifeCycleState: Lifecycle.State,
    crossinline collector: suspend (T) -> Unit = {}
) {
    lifecycleOwner.lifecycleScope.launchWhenStarted {
        delay(firstTimeDelay)
        lifecycleOwner.lifecycle.repeatOnLifecycle(onLifeCycleState) {
            collect(collector)
        }
    }
}

inline fun <reified T> SavedStateHandle.getMutableStateFlow(key: String, initialValue: T) = MutableStateFlow(get<T>(key) ?: initialValue)



@Suppress("UNCHECKED_CAST")
inline fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
        args[4] as T5,
        args[5] as T6
    )
}