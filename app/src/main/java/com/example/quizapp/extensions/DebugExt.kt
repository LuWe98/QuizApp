package com.example.quizapp.extensions

import android.util.Log

fun Any.log(text: String) {
    Log.e(javaClass.name, text)
}

val <T> T.exhaustive : T get() = this