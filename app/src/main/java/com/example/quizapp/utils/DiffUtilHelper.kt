package com.example.quizapp.utils

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

object DiffUtilHelper {

    fun <T> createDiffUtil(areItemsTheSameCheck: (T, T) -> (Boolean)): DiffUtil.ItemCallback<T> {
        return object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T) = areItemsTheSameCheck.invoke(oldItem, newItem)

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
        }
    }
}