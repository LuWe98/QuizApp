package com.example.quizapp.extensions

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.quizapp.R
import com.example.quizapp.view.QuizActivity
import com.example.quizapp.view.bindingsuperclasses.BindingActivity
import com.google.android.material.snackbar.Snackbar
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


@MainThread
fun BindingActivity<*>.showSnackBar(
    @StringRes textRes: Int,
    viewToAttachTo: View = rootView,
    anchorView: View? = null,
    animationMode: Int = Snackbar.ANIMATION_MODE_SLIDE,
    duration: Int = Snackbar.LENGTH_LONG) =
    showSnackBar(getString(textRes), viewToAttachTo, anchorView, animationMode, duration)

@MainThread
fun BindingActivity<*>.showSnackBar(
    text: String,
    viewToAttachTo: View = rootView,
    anchorView: View? = null,
    animationMode: Int = Snackbar.ANIMATION_MODE_SLIDE,
    duration: Int = Snackbar.LENGTH_LONG) =
    Snackbar.make(viewToAttachTo, text, duration).apply {
        setAnchorView(anchorView)
        this.animationMode = animationMode
        show()
    }.also {
        currentSnackBar = it
    }

@MainThread
inline fun BindingActivity<*>.showSnackBar(
    @StringRes textRes: Int,
    viewToAttachTo: View = rootView,
    anchorView: View? = null,
    animationMode: Int = Snackbar.ANIMATION_MODE_SLIDE,
    duration: Int = Snackbar.LENGTH_LONG,
    crossinline onDismissedAction: () -> (Unit) = {},
    @StringRes actionTextRes: Int,
    crossinline actionClickEvent: ((View) -> Unit)
) = Snackbar.make(viewToAttachTo, textRes, duration).apply {
    setAnchorView(anchorView)
    this.animationMode = animationMode
    addCallback(object : Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            if (event != DISMISS_EVENT_ACTION) { onDismissedAction.invoke() }
        }
    })
    setAction(actionTextRes) { actionClickEvent(it) }
    show()
}.also {
    currentSnackBar = it
}


val NavHostFragment.currentFragment get() : Fragment = childFragmentManager.fragments.last()

val QuizActivity.navHostFragment get() : NavHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

val QuizActivity.navController get() = navHostFragment.navController

val QuizActivity.currentNavHostFragment get() = navHostFragment.currentFragment
