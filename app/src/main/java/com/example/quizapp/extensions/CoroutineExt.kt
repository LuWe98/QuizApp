package com.example.quizapp.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

inline fun Any.launch(scope: CoroutineScope, crossinline block: suspend CoroutineScope.() -> Unit) {
    scope.launch {
        block.invoke(this)
    }
}

