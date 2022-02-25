package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviCourseOfStudiesChoiceBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCourseOfStudiesChoice : BindingListAdapter<CourseOfStudies, RviCourseOfStudiesChoiceBinding>(CourseOfStudies.DIFF_CALLBACK) {

    var onDeleteButtonClicked: ((CourseOfStudies) -> (Unit))? = null

    override fun initListeners(binding: RviCourseOfStudiesChoiceBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            btnRemove.onClick { onDeleteButtonClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviCourseOfStudiesChoiceBinding, item: CourseOfStudies, position: Int) {
        binding.apply {
            tvAbbreviation.text = item.abbreviation
            tvName.text = item.name
        }
    }
}