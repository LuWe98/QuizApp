package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviMenuBinding
import com.example.quizapp.extensions.context
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.model.menudatamodels.MenuItem
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaBsdfMenu : BindingListAdapter<MenuItem, RviMenuBinding>(MenuItem.DIFF_CALLBACK) {

    var onItemClicked: ((Int) -> (Unit))? = null

    override fun initListeners(binding: RviMenuBinding, vh: BindingListAdapterViewHolder) {
        binding.root.onClick {
            onItemClicked?.invoke(getItem(vh).id)
        }
    }

    override fun bindViews(binding: RviMenuBinding, item: MenuItem, position: Int) {
        binding.apply {
            title.text = context.getString(item.titleRes)
            icon.setImageDrawable(item.iconRes)
        }
    }
}