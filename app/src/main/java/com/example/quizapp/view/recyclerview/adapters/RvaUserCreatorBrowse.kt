package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviUserNewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter

class RvaUserCreatorBrowse : BindingPagingDataAdapter<User, RviUserNewBinding>(User.DIFF_CALLBACK) {

    var onItemClicked: ((User) -> Unit)? = null

    var selectionPredicate: ((User) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviUserNewBinding, vh: BindingPagingDataAdapterViewHolder) {
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

    override fun bindViews(binding: RviUserNewBinding, item: User, position: Int) {
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