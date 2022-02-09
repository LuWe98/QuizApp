package com.example.quizapp.extensions

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.sqrt
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

fun Number.nSqrt(n : Int) : Float {
    var newValue = this
    for(i in 0 .. n) {
        newValue = sqrt(newValue.toFloat())
    }
    return newValue.toFloat()
}

fun BottomSheetBehavior<*>.toggle(
    stateOne: Int = BottomSheetBehavior.STATE_EXPANDED,
    stateTwo: Int = BottomSheetBehavior.STATE_HIDDEN
) {
    if(state == stateOne) {
        state = stateTwo
    } else if(state == stateTwo) {
        state = stateOne
    }
}

//operator fun <A, B, C> ((A) -> B).div(getter : (B) -> C) : (A) -> C = { getter(this(it)) }
//
//infix fun <A, B, C> ((A) -> B).test(getter : (B) -> C) : (A) -> C = { getter(this(it)) }
