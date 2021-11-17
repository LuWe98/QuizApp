package com.example.quizapp.view.customimplementations.quizscreen.lazyquestiontab

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class LazyQuestionTabLayoutAdapter<T: Any, VH: LazyQuestionTabLayoutAdapter.LazyTabLayoutViewHolder<T>> (itemCallback: DiffUtil.ItemCallback<T>): ListAdapter<T, VH>(itemCallback) {

    abstract class LazyTabLayoutViewHolder <T: Any> (view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: T)
    }

}