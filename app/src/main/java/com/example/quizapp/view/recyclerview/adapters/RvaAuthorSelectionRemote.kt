package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviAuthorSelectionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter

class RvaAuthorSelectionRemote : BindingPagingDataAdapter<AuthorInfo, RviAuthorSelectionBinding>(AuthorInfo.DIFF_CALLBACK) {

    var onItemClicked: ((AuthorInfo) -> Unit)? = null

    var selectionPredicate: ((AuthorInfo) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviAuthorSelectionBinding, vh: BindingPagingDataAdapterViewHolder) {
        binding.apply {
            root.onClick { getItem(vh)?.let { onItemClicked?.invoke(it) } }
            root.onLongClick { getItem(vh)?.let { onItemClicked?.invoke(it) } }
        }
    }

    override fun bindViews(binding: RviAuthorSelectionBinding, item: AuthorInfo, position: Int) {
        binding.apply {
            roleIcon.setImageDrawable(R.drawable.ic_person)
            tvName.text = item.userName
            checkBox.isChecked = selectionPredicate.invoke(item)
        }
    }
}