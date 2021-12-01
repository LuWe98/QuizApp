package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviMenuBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.menus.SelectionTypeItemMarker
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaSelectionDialog : BindingListAdapter<SelectionTypeItemMarker<*>, RviMenuBinding>(SelectionTypeItemMarker.DIFF_CALLBACK) {

    var onItemClicked: ((SelectionTypeItemMarker<*>) -> (Unit))? = null

    var selectionPredicate: ((SelectionTypeItemMarker<*>) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviMenuBinding, vh: BindingListAdapterViewHolder) {
        binding.root.apply {
            onClick { onItemClicked?.invoke(getItem(vh)) }
            onLongClick { onItemClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviMenuBinding, item: SelectionTypeItemMarker<*>, position: Int) {
        binding.apply {
            title.text = context.getString(item.textRes)
            icon.setImageDrawable(item.iconRes)

            if(selectionPredicate.invoke(item) && selectionColor != null){
                root.setCardBackgroundColor(selectionColor!!)
            } else {
                root.setCardBackgroundColor(getColor(R.color.transparent))
            }
        }
    }
}