package com.example.quizapp.extensions

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.transition.Slide
import com.example.quizapp.R
import com.example.quizapp.view.bindingsuperclasses.BindingActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val Fragment.bindingActivity get() = requireActivity() as BindingActivity<*>


@MainThread
fun Fragment.showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
    requireContext().showToast(text, duration)
}

@MainThread
fun Fragment.showToast(@StringRes textRes: Int, duration: Int = Toast.LENGTH_LONG) {
    requireContext().showToast(textRes, duration)
}

fun Fragment.showAlertDialog(
    @StringRes titleRes: Int,
    @StringRes textRes: Int,
    @StringRes positiveButtonRes: Int,
    @StringRes negativeButtonRes: Int,
    positiveButtonClicked: ((DialogInterface) -> Unit)? = null,
    negativeButtonClicked: ((DialogInterface) -> Unit)? = null
) {
    AlertDialog.Builder(requireContext())
        .setTitle(getString(titleRes))
        .setMessage(textRes)
        .setPositiveButton(positiveButtonRes) { dialogInterface, _ -> positiveButtonClicked?.invoke(dialogInterface) }
        .setNegativeButton(negativeButtonRes) { dialogInterface, _ -> negativeButtonClicked?.invoke(dialogInterface) }
        .show()
}


@MainThread
fun Fragment.showSnackBar(
    text: String,
    viewToAttachTo: View = bindingActivity.rootView,
    anchorView: View? = null,
    animationMode: Int = Snackbar.ANIMATION_MODE_SLIDE,
    duration: Int = Snackbar.LENGTH_LONG) =
    Snackbar.make(viewToAttachTo, text, duration).apply {
        setAnchorView(anchorView)
        this.animationMode = animationMode
        show()
    }.also {
        bindingActivity.currentSnackBar = it
    }

@MainThread
fun Fragment.showSnackBar(
    @StringRes textRes: Int,
    viewToAttachTo: View = bindingActivity.rootView,
    anchorView: View? = null,
    animationMode: Int = Snackbar.ANIMATION_MODE_SLIDE,
    duration: Int = Snackbar.LENGTH_LONG) =
    showSnackBar(getString(textRes), viewToAttachTo, anchorView, animationMode, duration)

@MainThread
inline fun Fragment.showSnackBar(
    @StringRes textRes: Int,
    viewToAttachTo: View = bindingActivity.rootView,
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
    bindingActivity.currentSnackBar = it
}



fun Fragment.isPermissionGranted(permission: String) = requireContext().isPermissionGranted(permission)

fun Fragment.getDrawable(@DrawableRes id: Int) = AppCompatResources.getDrawable(requireContext(), id)

fun Fragment.getColor(@ColorRes id: Int) = ContextCompat.getColor(requireContext(), id)

fun Fragment.getColorStateListWithRes(@ColorRes id: Int): ColorStateList = AppCompatResources.getColorStateList(requireContext(), id)

fun Fragment.getColorStateList(@ColorInt colorInt: Int): ColorStateList = ColorStateList.valueOf(colorInt)

fun Fragment.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

inline fun Fragment.launch(
    dispatcher: CoroutineContext = EmptyCoroutineContext,
    scope: CoroutineScope = lifecycleScope,
    startDelay: Long = 0,
    crossinline block: suspend CoroutineScope.() -> Unit
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
    scope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            block.invoke(this)
        }
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
        ViewModelProvider(it, HiltViewModelFactory(requireContext(), it))[VM::class.java]
    }
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModels() = lazy {
    ViewModelProvider(requireActivity())[VM::class.java]
}


fun Fragment.showKeyboard(view: View) {
    requireContext().showKeyboard(view)
}

fun Fragment.hideKeyboard(view: View) {
    requireContext().hideKeyboard(view)
}





//ANIMATION EXTENSIONS
fun Fragment.initContainerTransitionAnimation(
    parsedStartView: View,
    animationDuration: Long = resources.getInteger(R.integer.defaultAnimDuration).toLong()
) {
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
        drawingViewId = R.id.navHost
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