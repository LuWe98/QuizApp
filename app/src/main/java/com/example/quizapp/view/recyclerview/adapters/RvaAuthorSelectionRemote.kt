package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviUserBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter

class RvaAuthorSelectionRemote : BindingPagingDataAdapter<AuthorInfo, RviUserBinding>(AuthorInfo.DIFF_CALLBACK) {

    var onItemClicked: ((AuthorInfo) -> Unit)? = null

    var selectionPredicate: ((AuthorInfo) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviUserBinding, vh: BindingPagingDataAdapterViewHolder) {
        binding.apply {
            root.onClick {
                getItem(vh)?.let {
                    onItemClicked?.invoke(it.copy())
                }
            }

            root.onLongClick {
                getItem(vh)?.let {
                    onItemClicked?.invoke(it.copy())
                }
            }
        }
    }

    override fun bindViews(binding: RviUserBinding, item: AuthorInfo, position: Int) {
        binding.apply {
            roleIcon.setImageDrawable(R.drawable.ic_person)
            tvName.text = item.userName

            if(selectionPredicate.invoke(item) && selectionColor != null){
                startCard.setBackgroundTint(getThemeColor(R.attr.colorPrimary))
                roleIcon.setDrawableTint(getColor(R.color.white))
            } else {
                startCard.setBackgroundTintWithRes(defaultBackgroundColor)
                roleIcon.setDrawableTint(getThemeColor(R.attr.colorControlNormal))
            }
        }
    }
}