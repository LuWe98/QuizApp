package com.example.quizapp.view.recyclerview.impl

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.quizapp.utils.BindingUtils

abstract class BindingPagingDataAdapter<T : Any, B : ViewBinding>(diffCallback: DiffUtil.ItemCallback<T>) :
    PagingDataAdapter<T, BindingPagingDataAdapter<T, B>.BindingPagingDataAdapterViewHolder>(diffCallback) {

    override fun onBindViewHolder(vh: BindingPagingDataAdapterViewHolder, position: Int) {
        getItem(position)?.let { vh.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BindingPagingDataAdapterViewHolder(BindingUtils.getViewHolderBindingWith(this, parent))

    inner class BindingPagingDataAdapterViewHolder(val binding: B) : BindingViewHolder<T>(binding) {
        init {
            initListeners(binding, this)
        }

        override fun bind(item: T) {
            bindViews(binding, item, bindingAdapterPosition)
        }
    }



    fun getItem(viewHolder: RecyclerView.ViewHolder): T? = getItem(viewHolder.bindingAdapterPosition)

    abstract fun initListeners(binding: B, vh: BindingPagingDataAdapterViewHolder)

    abstract fun bindViews(binding: B, item: T, position: Int)

}