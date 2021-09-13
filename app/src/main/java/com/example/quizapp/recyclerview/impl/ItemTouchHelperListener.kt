package com.example.quizapp.recyclerview.impl

interface ItemTouchHelperListener {
    fun onSwiped(position : Int)

    fun onDrag(fromPosition: Int, toPosition : Int)

    fun onDragReleased()
}