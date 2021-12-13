package com.example.quizapp.extensions

import android.app.Activity
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.transition.Slide
import com.example.quizapp.R
import com.example.quizapp.model.selection.SelectionTypeItemMarker
import com.example.quizapp.view.bindingsuperclasses.BindingActivity
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    duration: Int = Snackbar.LENGTH_LONG
) = Snackbar.make(viewToAttachTo, text, duration).apply {
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
    duration: Int = Snackbar.LENGTH_LONG
) = showSnackBar(getString(textRes), viewToAttachTo, anchorView, animationMode, duration)

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
            if (event != DISMISS_EVENT_ACTION) {
                onDismissedAction.invoke()
            }
        }
    })
    setAction(actionTextRes) { actionClickEvent(it) }
    show()
}.also {
    bindingActivity.currentSnackBar = it
}


fun Fragment.isPermissionGranted(permission: String) = requireContext().isPermissionGranted(permission)

fun Fragment.askForPermission(action: (Boolean) -> (Unit)) = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    action.invoke(granted)
}

fun Fragment.startDocumentFilePickerResult(action: (DocumentFile?) -> Unit) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        result.data?.let { intent ->
            if (DocumentFile.isDocumentUri(requireContext(), intent.data)) {
                DocumentFile.fromSingleUri(requireContext(), intent.data!!)?.let { documentFile ->
                    action.invoke(if (!documentFile.isFile) null else documentFile)
                    return@registerForActivityResult
                }
            }
            action.invoke(null)
        }
    }
}


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
    dispatcher: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launch(context = dispatcher) {
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
    getHiltNavDestinationViewModel<VM>(destinationId)
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.getHiltNavDestinationViewModel(
    @IdRes destinationId: Int
) = findNavController().getBackStackEntry(destinationId).let {
    ViewModelProvider(it, HiltViewModelFactory(requireContext(), it))[VM::class.java]
}


@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModels() = lazy {
    getActivityViewModel<VM>()
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.getActivityViewModel() = ViewModelProvider(requireActivity())[VM::class.java]


fun Fragment.showKeyboard(view: View) {
    requireContext().showKeyboard(view)
}

fun Fragment.hideKeyboard(view: View) {
    requireContext().hideKeyboard(view)
}


inline fun <reified T : DialogFragment> Fragment.showDialog(tag: String? = null, fragmentManager: FragmentManager = childFragmentManager): T {
    return T::class.java.newInstance().apply {
        show(fragmentManager, tag)
    }
}

inline fun <reified T : DialogFragment> Fragment.findDialog(tag: String, fragmentManager: FragmentManager = childFragmentManager): T? {
    return fragmentManager.findFragmentByTag(tag) as T?
}


/**
 * Listener for various selection types -> Acts as a wrapper for setFragmentResultListener.
 * Possible SelectionTypes can be seen in the [SelectionType] sealed class.
 * It supports selection that only requires the user input and listens for the selection with the according resultKey
 */
inline fun <reified ResultType : SelectionTypeItemMarker<ResultType>> Fragment.setSelectionTypeListener(
    crossinline action: (ResultType) -> (Unit)
) {
    SelectionType.getResultKeyWithResultClass<ResultType>().let { resultKey ->
        setFragmentResultListener(resultKey) { _, bundle ->
            bundle.getParcelable<ResultType>(resultKey)?.let {
                action.invoke(it)
            }
        }
    }
}

/**
 * Listener for various selection types -> Acts as a wrapper for setFragmentResultListener.
 * Possible SelectionTypes can be seen in the [SelectionType] sealed class.
 * It supports selection that only requires the user input and listens for the selection with the according resultKey.
 * This version of the method also returns the used SelectionType instance
 */
inline fun <reified ResultType : SelectionTypeItemMarker<ResultType>, reified SelectionTypeT : SelectionType> Fragment.setSelectionTypeWithParsedValueListener(
    crossinline action: (ResultType, SelectionTypeT) -> (Unit)
) {
    SelectionType.getResultKeyWithResultClass<ResultType>().let { resultKey ->
        setFragmentResultListener(resultKey) { _, bundle ->
            bundle.getParcelable<ResultType>(resultKey)?.let { result ->
                bundle.getParcelable<SelectionTypeT>(resultKey.plus(SelectionType.INITIAL_VALUE_SUFFIX))?.let { parsedValue ->
                    action.invoke(result, parsedValue)
                }
            }
        }
    }
}

/**
 * Listener for the [com.example.quizapp.view.fragments.dialogs.stringupdatedialog.DfUpdateString] -> Acts as a wrapper for setFragmentResultListener.
 * Possible update types can be seen in the [UpdateStringType] enum class.
 * It supports user String input which will be returned after confirmation with the given resultKey
 */
inline fun Fragment.setUpdateStringTypeListener(type: UpdateStringType, crossinline action: (String) -> (Unit)) {
    setFragmentResultListener(type.resultKey) { key, bundle ->
        bundle.getString(key)?.let {
            action.invoke(it)
        }
    }
}


/**
 * Listener for various confirmation types -> Acts as a wrapper for setFragmentResultListener.
 * Possible SelectionTypes can be seen in the [ConfirmationType] sealed class.
 * It supports confirmation of the user with one positive and one negative button.
 */
inline fun <reified ResultType : ConfirmationType> Fragment.setConfirmationTypeListener(crossinline action: (ResultType) -> (Unit)) {
    ConfirmationType.getResultKeyWithResultType<ResultType>().let { resultKey ->
        setFragmentResultListener(resultKey) { _, bundle ->
            bundle.getParcelable<ResultType>(resultKey)?.let {
                action.invoke(it)
            }
        }
    }
}




//action.invoke(SelectionType.getBundleContent<SelectionTypeClass, ResultType>(resultKey, bundle))


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



fun Fragment.initMaterialZAxisAnimationForCaller() {
    exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
        duration = resources.getInteger(R.integer.defaultAnimDuration).toLong()
    }
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
        duration = resources.getInteger(R.integer.defaultAnimDuration).toLong()
    }
}

fun Fragment.initMaterialZAxisAnimationForReceiver(){
    enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
        duration = resources.getInteger(R.integer.defaultAnimDuration).toLong()
    }
    returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
        duration = resources.getInteger(R.integer.defaultAnimDuration).toLong()
    }
}
