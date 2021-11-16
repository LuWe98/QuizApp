package com.example.quizapp.extensions


operator fun <A, B, C> ((A) -> B).div(getter : (B) -> C) : (A) -> C = { getter(this(it)) }

infix fun <A, B, C> ((A) -> B).test(getter : (B) -> C) : (A) -> C = { getter(this(it)) }
