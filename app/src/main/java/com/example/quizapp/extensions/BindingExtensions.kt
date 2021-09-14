package com.example.quizapp.extensions

import android.content.Context
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding

val ViewBinding.context : Context get() = root.context

fun ViewBinding.getDrawable(@DrawableRes res : Int) = ContextCompat.getDrawable(context, res)

fun ViewBinding.getColor(@ColorRes res : Int) = ContextCompat.getColor(context, res)

fun ViewBinding.getThemeColor(@AttrRes res : Int) = context.getThemeColor(res)