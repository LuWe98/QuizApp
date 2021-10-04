package com.example.quizapp.extensions

import android.util.Log
import com.example.quizapp.BuildConfig

enum class LogType{
    Debug,
    Error,
    Wtf
}

fun Any.log(text: String, logType: LogType = LogType.Debug) {
    when(logType){
        LogType.Debug -> Log.d(tag ,  text)
        LogType.Error -> Log.e(tag ,  text)
        LogType.Wtf -> Log.wtf(tag ,  text)
    }
}

private val Any.tag get() = "${BuildConfig.APPLICATION_ID} - ${javaClass.simpleName}"

val <T> T.exhaustive : T get() = this