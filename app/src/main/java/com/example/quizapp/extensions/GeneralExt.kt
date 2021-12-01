package com.example.quizapp.extensions

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import kotlin.reflect.KProperty1

inline operator fun <reified A, reified B, reified C> KProperty1<A, B>.div(crossinline getter: (B) -> C): (A) -> C = {
    getter(this(it))
}

inline infix fun <reified A, reified B, reified C> KProperty1<A, B>.test(crossinline getter: (B) -> C): (A) -> C = {
    getter(this(it))
}

inline fun <reified T, reified P> generateDiffItemCallback(crossinline idProvider: ((T) -> (P))) = object : DiffUtil.ItemCallback<T>() {
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
    override fun areItemsTheSame(oldItem: T, newItem: T) = idProvider.invoke(oldItem) == idProvider.invoke(newItem)
}

//operator fun <A, B, C> ((A) -> B).div(getter : (B) -> C) : (A) -> C = { getter(this(it)) }
//
//infix fun <A, B, C> ((A) -> B).test(getter : (B) -> C) : (A) -> C = { getter(this(it)) }


