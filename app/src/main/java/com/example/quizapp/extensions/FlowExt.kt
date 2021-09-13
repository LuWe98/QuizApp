package com.example.quizapp.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun <T> Flow<T>.collectLatest(scope : CoroutineScope, action: (T) -> Unit) {
    scope.launch {
        collectLatest {
            action.invoke(it)
        }
    }
}

fun <T> Flow<T>.collect(scope : CoroutineScope, action: (T) -> Unit) {
    scope.launch {
        collect {
            action.invoke(it)
        }
    }
}

fun <T> Flow<T>.first(scope : CoroutineScope) : T {
    return runBlocking(scope.coroutineContext) {
        first()
    }
}

fun <T> Flow<T>.firstOrNull(scope : CoroutineScope) : T? {
    return runBlocking(scope.coroutineContext) {
        firstOrNull()
    }
}