package com.example.quizapp.recyclerview.impl

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BindingViewHolder<V>(bindingVariable: ViewBinding) : RecyclerView.ViewHolder(bindingVariable.root) {
    abstract fun bind(item : V)
}