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
    viewLifecycleOwner: LifecycleOwner,
    firstTimeDelay: Long = 0L,
    onLifeCycleState: Lifecycle.State,
    crossinline collector: suspend (T) -> Unit = {}
) {
    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        delay(firstTimeDelay)
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(onLifeCycleState) {
            collect(collector)
        }
    }
}

inline fun <reified T> SavedStateHandle.getMutableStateFlow(key: String, initialValue: T) = MutableStateFlow(get<T>(key) ?: initialValue)