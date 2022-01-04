package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviFacultyChoiceBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaFacultyChoice : BindingListAdapter<Faculty, RviFacultyChoiceBinding>(Faculty.DIFF_CALLBACK) {

    var onDeleteButtonClicked: ((Faculty) -> (Unit))? = null

    override fun initListeners(binding: RviFacultyChoiceBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            btnRemove.onClick { onDeleteButtonClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviFacultyChoiceBinding, item: Faculty, position: Int) {
        binding.apply {
            tvAbbreviation.text = item.abbreviation
            tvName.text = item.name
        }
    }
}