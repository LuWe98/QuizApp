package com.example.quizapp.extensions

import android.app.Activity
import android.graphics.Color
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun Activity.enableFullScreenMode() {
    window.apply {
        WindowCompat.setDecorFitsSystemWindows(this, false)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor = Color.TRANSPARENT
    }
}

inline fun AppCompatActivity.launch(
    dispatcher: CoroutineContext = EmptyCoroutineContext,
    scope: CoroutineScope = lifecycleScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launch(dispatcher) {
        block.invoke(this)
    }
}

inline fun AppCompatActivity.launchWhenStarted(
    scope: LifecycleCoroutineScope = lifecycleScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launchWhenStarted {
        block.invoke(this)
    }
}

inline fun AppCompatActivity.launchWhenCreated(
    scope: LifecycleCoroutineScope = lifecycleScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launchWhenCreated {
        block.invoke(this)
    }
}

inline fun AppCompatActivity.launchWhenResumed(
    scope: LifecycleCoroutineScope = lifecycleScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launchWhenResumed {
        block.invoke(this)
    }
}

inline fun AppCompatActivity.launchDelayed(
    scope: CoroutineScope = lifecycleScope,
    dispatcher: CoroutineContext = EmptyCoroutineContext,
    startDelay: Long,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launch(dispatcher) {
        delay(startDelay)
        block.invoke(this)
    }
}