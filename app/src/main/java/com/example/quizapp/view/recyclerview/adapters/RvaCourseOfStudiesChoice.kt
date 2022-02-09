package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviCourseOfStudiesChoiceNewBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCourseOfStudiesChoice : BindingListAdapter<CourseOfStudies, RviCourseOfStudiesChoiceNewBinding>(CourseOfStudies.DIFF_CALLBACK) {

    var onDeleteButtonClicked: ((CourseOfStudies) -> (Unit))? = null

    override fun initListeners(binding: RviCourseOfStudiesChoiceNewBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            btnRemove.onClick { onDeleteButtonClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviCourseOfStudiesChoiceNewBinding, item: CourseOfStudies, position: Int) {
        binding.apply {
            tvAbbreviation.text = item.abbreviation
            tvName.text = item.name
        }
    }
}