package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviAuthorChoiceBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaAuthorChoice : BindingListAdapter<AuthorInfo, RviAuthorChoiceBinding>(AuthorInfo.DIFF_CALLBACK) {

    var onDeleteButtonClicked: ((AuthorInfo) -> (Unit))? = null

    override fun initListeners(binding: RviAuthorChoiceBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            btnRemove.onClick { onDeleteButtonClicked?.invoke(getItem(vh).copy()) }
        }
    }

    override fun bindViews(binding: RviAuthorChoiceBinding, item: AuthorInfo, position: Int) {
        binding.apply {
            tvName.text = item.userName
        }
    }
}