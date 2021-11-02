package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviFacultyBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaFaculty : BindingListAdapter<Faculty, RviFacultyBinding>(Faculty.DIFF_CALLBACK) {

    var onItemClicked: ((Faculty) -> (Unit))? = null

    override fun initListeners(binding: RviFacultyBinding, vh: BindingListAdapterViewHolder) {
        binding.root.onClick {
            getItem(vh).let {
                onItemClicked?.invoke(it)
            }
        }
    }

    override fun bindViews(binding: RviFacultyBinding, item: Faculty, position: Int) {
        binding.apply {
            tvAbbreviation.text = item.abbreviation
            tvName.text = item.name
        }
    }
}