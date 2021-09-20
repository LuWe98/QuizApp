package com.example.quizapp.model.room.entities

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil

abstract class EntityMarker(
    open val id: Long
) : Parcelable {

    companion object {
        fun <T : EntityMarker> createBasicDiffUtil() = object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
        }
    }

}