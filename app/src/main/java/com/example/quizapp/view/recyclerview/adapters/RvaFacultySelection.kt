package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviCourseOfStudiesBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaFacultySelection : BindingListAdapter<Faculty, RviCourseOfStudiesBinding>(Faculty.DIFF_CALLBACK) {

    var onItemClicked: ((String) -> (Unit))? = null

    var selectionPredicate: ((Faculty) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviCourseOfStudiesBinding, vh: BindingListAdapterViewHolder) {
        binding.root.onClick {
            onItemClicked?.invoke(getItem(vh).id)
        }
    }

    override fun bindViews(binding: RviCourseOfStudiesBinding, item: Faculty, position: Int) {
        binding.apply {
            tvAbbreviation.text = item.abbreviation
            tvName.text = item.name

            if(selectionPredicate.invoke(item) && selectionColor != null){
                root.setCardBackgroundColor(selectionColor!!)
                tvAbbreviation.setBackgroundTint(getThemeColor(R.attr.colorPrimary))
                tvAbbreviation.setTextColor(getColor(R.color.white))
            } else {
                root.setCardBackgroundColor(getColor(R.color.transparent))
                tvAbbreviation.setBackgroundTintWithRes(defaultBackgroundColor)
                tvAbbreviation.setTextColor(getThemeColor(R.attr.defaultTextColor))
            }
        }
    }
}