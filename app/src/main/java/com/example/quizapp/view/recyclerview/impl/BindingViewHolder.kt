package com.example.quizapp.view.recyclerview.impl

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BindingViewHolder<V>(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(item : V)
}