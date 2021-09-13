package com.example.quizapp.extensions

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.Slide
import com.example.quizapp.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@MainThread
fun Fragment.showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
    requireContext().showToast(text, duration)
}

@MainThread
fun Fragment.showToast(@StringRes textRes: Int, duration: Int = Toast.LENGTH_LONG) {
    requireContext().showToast(textRes, duration)
}

@MainThread
fun Fragment.showSnackBar(text: String, viewToAttachTo: View = requireView(), duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(viewToAttachTo, text, duration).show()
}

@MainThread
fun Fragment.showSnackBar(@StringRes textRes: Int, viewToAttachTo: View = requireView(), duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(viewToAttachTo, textRes, duration).show()
}

@MainThread
fun Fragment.showSnackBar(
    text: String,
    viewToAttachTo: View = requireView(),
    duration: Int = Snackbar.LENGTH_LONG,
    actionText: String,
    actionClickEvent: ((View) -> Unit)
) {
    Snackbar.make(viewToAttachTo, text, duration).setAction(actionText, actionClickEvent).show()
}

@MainThread
fun Fragment.showSnackBar(
    @StringRes textRes: Int,
    viewToAttachTo: View = requireView(),
    duration: Int = Snackbar.LENGTH_LONG,
    @StringRes actionTextRes: Int,
    actionClickEvent: ((View) -> Unit)
) {
    Snackbar.make(viewToAttachTo, textRes, duration).setAction(actionTextRes, actionClickEvent).show()
}

fun Fragment.isPermissionGranted(permission: String) = requireContext().isPermissionGranted(permission)

fun Fragment.getDrawable(@DrawableRes id: Int) = AppCompatResources.getDrawable(requireContext(), id)

fun Fragment.getColor(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)

fun Fragment.getColorStateList(@ColorRes id: Int) : ColorStateList = AppCompatResources.getColorStateList(requireContext(), id)

fun Fragment.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

fun Fragment.getThemeColor(@AttrRes themeAttrId: Int) = requireContext().getThemeColor(themeAttrId)

fun Fragment.initVoiceSearch(actionToPerform: (String) -> (Unit)) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        result.data?.apply {
            actionToPerform.invoke(getStringExtra(RecognizerIntent.EXTRA_RESULTS) ?: "")
        }
    }
}


inline fun Fragment.launch(
    scope: CoroutineScope = lifecycleScope,
    dispatcher: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launch(dispatcher) {
        block.invoke(this)
    }
}

inline fun Fragment.launchDelayed(
    scope: CoroutineScope = lifecycleScope,
    dispatcher: CoroutineContext = EmptyCoroutineContext,
    startDelay: Long, crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launch(dispatcher) {
        delay(startDelay)
        block.invoke(this)
    }
}

inline fun Fragment.launchWhenStarted(
    scope: LifecycleCoroutineScope = lifecycleScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launchWhenStarted {
        block.invoke(this)
    }
}

inline fun Fragment.launchWhenCreated(
    scope: LifecycleCoroutineScope = lifecycleScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launchWhenCreated {
        block.invoke(this)
    }
}

inline fun Fragment.launchWhenResumed(
    scope: LifecycleCoroutineScope = lifecycleScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launchWhenResumed {
        block.invoke(this)
    }
}


@MainThread
inline fun <reified VM : ViewModel> Fragment.hiltNavDestinationViewModels(
    @IdRes destinationId: Int
) = lazy {
    findNavController().getBackStackEntry(destinationId).let {
        ViewModelProvider(it, HiltViewModelFactory(requireContext(), it)).get(VM::class.java)
    }
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModel() = lazy {
    ViewModelProvider(requireActivity()).get(VM::class.java)
}




//ANIMATION EXTENSIONS
fun Fragment.initContainerTransitionAnimation(parsedStartView: View, animationDuration: Long = resources.getInteger(R.integer.defaultAnimDuration).toLong()) {
    enterTransition = MaterialContainerTransform(requireContext(), true).apply {
        startView = parsedStartView
        endView = view
        duration = animationDuration
        scrimColor = Color.TRANSPARENT
        containerColor = getThemeColor(R.attr.backgroundColor)
        startContainerColor = getColor(R.color.black)
        endContainerColor = getThemeColor(R.attr.backgroundColor)
    }
    returnTransition = Slide().apply {
        duration = animationDuration
        view?.id?.let { addTarget(it) }
    }
}

fun Fragment.initSharedElementTransitionAnimation(animationDuration: Long = resources.getInteger(R.integer.defaultAnimDuration).toLong()) {
    sharedElementEnterTransition = MaterialContainerTransform().apply {
        drawingViewId = R.id.mainActivityNavHost
        duration = animationDuration
        scrimColor = Color.TRANSPARENT
        setAllContainerColors(getThemeColor(R.attr.backgroundColor))
    }

    returnTransition = Slide().apply {
        duration = animationDuration
        view?.id?.let { addTarget(it) }
    }
}

fun Fragment.initMaterialFadeIn(animationDuration: Long = resources.getInteger(R.integer.defaultAnimDuration).toLong()) {
    enterTransition = MaterialFadeThrough().apply {
        duration = animationDuration
    }
}

fun Fragment.initMaterialFadeOut(animationDuration: Long = resources.getInteger(R.integer.defaultAnimDuration).toLong()) {
    enterTransition = MaterialFadeThrough().apply {
        duration = animationDuration
    }
}

fun Fragment.initMaterialFade(animationDuration: Long = resources.getInteger(R.integer.defaultAnimDuration).toLong()) {
    initMaterialFadeIn(animationDuration)
    initMaterialFadeOut(animationDuration)
}

fun Fragment.initMaterialElevationScale(animationDuration: Long = resources.getInteger(R.integer.defaultAnimDuration).toLong()) {
    exitTransition = MaterialElevationScale(false).apply {
        duration = animationDuration
    }

    reenterTransition = MaterialElevationScale(true).apply {
        duration = animationDuration
    }

    enterTransition = MaterialElevationScale(true).apply {
        duration = animationDuration
    }
}