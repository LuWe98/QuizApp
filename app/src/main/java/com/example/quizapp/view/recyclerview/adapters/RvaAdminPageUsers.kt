package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviUserBinding
import com.example.quizapp.model.mongodb.documents.user.User
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter

class RvaAdminPageUsers : BindingPagingDataAdapter<User, RviUserBinding>(User.DIFF_CALLBACK) {

    override fun initListeners(binding: RviUserBinding, vh: BindingPagingDataAdapterViewHolder) {

    }

    override fun bindViews(binding: RviUserBinding, item: User, position: Int) {
        binding.apply {
            tvName.text = item.userName
            tvRole.text = item.role.name
        }
    }
}