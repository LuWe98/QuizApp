package com.example.quizapp.view.recyclerview.impl

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.quizapp.utils.BindingUtils

abstract class BindingListAdapter<T : Any, B : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>,
    val relativeVbPosition: Int = 0
) : ListAdapter<T, BindingListAdapter<T, B>.BindingListAdapterViewHolder>(diffCallback) {

    override fun onBindViewHolder(vh: BindingListAdapterViewHolder, position: Int) {
        getItem(position)?.let { vh.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BindingListAdapterViewHolder(BindingUtils.getViewHolderBindingWith(this, parent, relativeVbPosition))

    inner class BindingListAdapterViewHolder(private val binding: B) : BindingViewHolder<T>(binding) {
        init {
            initListeners(binding, this)
        }

        override fun bind(item: T) {
            bindViews(binding, item, bindingAdapterPosition)
        }
    }

    fun getItem(viewHolder: RecyclerView.ViewHolder): T = getItem(viewHolder.bindingAdapterPosition)

    abstract fun initListeners(binding: B, vh: BindingListAdapterViewHolder)

    abstract fun bindViews(binding: B, item: T, position: Int)

    fun moveItem(fromPosition : Int, toPosition : Int){
        submitList(currentList.toMutableList().apply {
            add(toPosition, removeAt(fromPosition))
        })
    }
}