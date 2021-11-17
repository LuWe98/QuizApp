package com.example.quizapp.extensions

import kotlin.reflect.KProperty1

inline operator fun <reified A, reified B, reified C> KProperty1<A, B>.div(crossinline getter: (B) -> C): (A) -> C = {
    getter(this(it))
}

inline infix fun <reified A, reified B, reified C> KProperty1<A, B>.test(crossinline getter: (B) -> C): (A) -> C = {
    getter(this(it))
}


//operator fun <A, B, C> ((A) -> B).div(getter : (B) -> C) : (A) -> C = { getter(this(it)) }
//
//infix fun <A, B, C> ((A) -> B).test(getter : (B) -> C) : (A) -> C = { getter(this(it)) }


