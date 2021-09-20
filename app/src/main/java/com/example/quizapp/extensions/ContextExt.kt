package com.example.quizapp.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.widget.Toast

import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

@MainThread
fun Context.showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, text, duration).show()
}

@MainThread
fun Context.showToast(@StringRes textRes: Int, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, textRes, duration).show()
}

val Context.statusBarHeight: Int get() = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))

fun Context.isPermissionGranted(permission: String) = checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED


fun Context.getThemeColor(@AttrRes themeAttrId: Int) = TypedValue().let {
    theme.resolveAttribute(themeAttrId, it, true)
    it.data
}

fun Context.showKeyboard(view : View) {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Context.hideKeyboard(view : View) {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(view.windowToken, 0)
    }
}