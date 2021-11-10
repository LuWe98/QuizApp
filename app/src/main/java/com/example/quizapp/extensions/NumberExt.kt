package com.example.quizapp.extensions

import android.content.res.Resources

val Number.px get() = (toFloat() / Resources.getSystem().displayMetrics.density).toInt()

val Number.dp get() = (toFloat() * Resources.getSystem().displayMetrics.density).toInt()