package com.example.quizapp.extensions

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.quizapp.R

val defaultBackgroundColor get() = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) R.color.darkBackgroundColor else R.color.lightBackgroundColor

fun Context.getThemeColor(@AttrRes themeAttrId: Int) = TypedValue().let {
    theme.resolveAttribute(themeAttrId, it, true)
    it.data
}

fun Fragment.getThemeColor(@AttrRes themeAttrId: Int) = requireContext().getThemeColor(themeAttrId)

