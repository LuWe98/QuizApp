package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviMenuBinding
import com.example.quizapp.extensions.context
import com.example.quizapp.extensions.getColor
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.model.menus.MenuStringIdItem
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaStringIdMenu : BindingListAdapter<MenuStringIdItem, RviMenuBinding>(MenuStringIdItem.DIFF_CALLBACK) {

    var onItemClicked: ((String) -> (Unit))? = null

    var selectionPredicate: ((MenuStringIdItem) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviMenuBinding, vh: BindingListAdapterViewHolder) {
        binding.root.onClick {
            onItemClicked?.invoke(getItem(vh).id)
        }
    }

    override fun bindViews(binding: RviMenuBinding, item: MenuStringIdItem, position: Int) {
        binding.apply {
            title.text = context.getString(item.titleRes)
            icon.setImageDrawable(item.iconRes)

            if(selectionPredicate.invoke(item) && selectionColor != null){
                root.setCardBackgroundColor(selectionColor!!)
            } else {
                root.setCardBackgroundColor(getColor(R.color.transparent))
            }
        }
    }
}