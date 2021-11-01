package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.R
import com.example.quizapp.databinding.RviUserBinding
import com.example.quizapp.extensions.context
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onLongClick
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter

class RvaAdminPageUsers : BindingPagingDataAdapter<User, RviUserBinding>(User.DIFF_CALLBACK) {

    var onItemClicked: ((User) -> Unit)? = null

    var onItemLongClicked: ((User) -> Unit)? = null

    override fun initListeners(binding: RviUserBinding, vh: BindingPagingDataAdapterViewHolder) {
        binding.apply {

            root.onClick {
                getItem(vh)?.let {
                    onItemClicked?.invoke(it)
                }
            }

            root.onLongClick {
                getItem(vh)?.let {
                    onItemLongClicked?.invoke(it.copy())
                }
            }
        }
    }

    override fun bindViews(binding: RviUserBinding, item: User, position: Int) {
        binding.apply {
            if(item.lastModifiedTimestamp == UNKNOWN_TIMESTAMP) hideLayout(this)
            else showLayout(this)

            tvName.text = item.userName
            tvRole.text = item.role.name
        }
    }

    private fun hideLayout(binding: RviUserBinding) = binding.apply {
        root.isVisible = false
        root.layoutParams = (root.layoutParams as RecyclerView.LayoutParams).apply {
            height = 0
            setMargins(0, 0,0,0)
        }
    }

    private fun showLayout(binding: RviUserBinding) = binding.apply {
        root.isVisible = true
        root.layoutParams = (root.layoutParams as RecyclerView.LayoutParams).apply {
            height = RecyclerView.LayoutParams.WRAP_CONTENT
            setMargins(0, context.resources.getDimension(R.dimen.grid_3).toInt(),0,0)
        }
    }



    fun updateUserRole(userId: String, newRole: Role) {
        snapshot().indexOfFirst { it?.id == userId }.let { index ->
            snapshot()[index]?.role = newRole
            notifyItemChanged(index)
        }
    }

    fun hideUser(userId: String) {
        snapshot().indexOfFirst { it?.id == userId }.let { index ->
            snapshot()[index]?.lastModifiedTimestamp = UNKNOWN_TIMESTAMP
            notifyItemChanged(index)
        }
    }

    fun showUser(user: User) {
        snapshot().indexOfFirst { it?.id == user.id }.let { index ->
            snapshot()[index]?.lastModifiedTimestamp = user.lastModifiedTimestamp
            notifyItemChanged(index)
        }
    }

    companion object {
        const val UNKNOWN_TIMESTAMP = -1L
    }
}