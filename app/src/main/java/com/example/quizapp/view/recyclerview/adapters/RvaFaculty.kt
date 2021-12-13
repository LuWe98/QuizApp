package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviFacultyBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onLongClick
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaFaculty : BindingListAdapter<Faculty, RviFacultyBinding>(Faculty.DIFF_CALLBACK) {

    var onItemClicked: ((Faculty) -> (Unit))? = null

    var onItemLongClicked: ((Faculty) -> (Unit))? = null

    override fun initListeners(binding: RviFacultyBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick { onItemClicked?.invoke(getItem(vh)) }

            root.onLongClick { onItemLongClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviFacultyBinding, item: Faculty, position: Int) {
        binding.apply {
            tvAbbreviation.text = item.abbreviation
            tvName.text = item.name
        }
    }
}