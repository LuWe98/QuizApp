package com.example.quizapp.extensions

import android.util.Log
import com.example.quizapp.BuildConfig

fun Any.log(text: String) {
    Log.e("${BuildConfig.APPLICATION_ID} - ${javaClass.simpleName}" ,  text)
}

val <T> T.exhaustive : T get() = this