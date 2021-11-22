package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviCourseOfStudiesBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onLongClick
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCourseOfStudies : BindingListAdapter<CourseOfStudies, RviCourseOfStudiesBinding>(CourseOfStudies.DIFF_CALLBACK) {

    var onItemClicked: ((CourseOfStudies) -> (Unit))? = null

    override fun initListeners(binding: RviCourseOfStudiesBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick { onItemClicked?.invoke(getItem(vh)) }
            root.onLongClick { onItemClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviCourseOfStudiesBinding, item: CourseOfStudies, position: Int) {
        binding.apply {
            tvAbbreviation.text = item.abbreviation
            tvName.text = item.name
        }
    }
}