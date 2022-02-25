package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.R
import com.example.quizapp.databinding.RviUserBinding
import com.example.quizapp.extensions.context
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onLongClick
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter

class RvaAdminUser : BindingPagingDataAdapter<User, RviUserBinding>(User.DIFF_CALLBACK) {

    var onItemClicked: ((User) -> Unit)? = null

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

    override fun bindViews(binding: RviUserBinding, item: User, position: Int) {
        binding.apply {
            if(item.lastModifiedTimestamp == User.UNKNOWN_TIMESTAMP) hideLayout(this)
            else showLayout(this)

            when(item.role) {
                Role.ADMIN -> roleIcon.setImageDrawable(R.drawable.ic_admin_panel)
                Role.CREATOR -> roleIcon.setImageDrawable(R.drawable.ic_edit)
                Role.USER -> roleIcon.setImageDrawable(R.drawable.ic_person)
            }
            tvName.text = item.name
        }
    }

    private fun hideLayout(binding: RviUserBinding) = binding.apply {
        root.isVisible = false
        root.updateLayoutParams<RecyclerView.LayoutParams> {
            height = 0
            setMargins(0, 0,0,0)
        }
    }

    private fun showLayout(binding: RviUserBinding) = binding.apply {
        root.isVisible = true
        root.updateLayoutParams<RecyclerView.LayoutParams> {
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
            snapshot()[index]?.lastModifiedTimestamp = User.UNKNOWN_TIMESTAMP
            notifyItemChanged(index)
        }
    }
}