package com.example.quizapp.view.customimplementations.quizscreen.lazytablayout

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LazyQuestionTabLayoutManager(context: Context)  : LinearLayoutManager(context, HORIZONTAL, false)  {

    override fun generateDefaultLayoutParams() = RecyclerView.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    override fun canScrollVertically() = false
    override fun canScrollHorizontally() = true
}
