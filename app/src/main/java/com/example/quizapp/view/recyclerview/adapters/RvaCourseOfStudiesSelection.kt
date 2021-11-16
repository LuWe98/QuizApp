package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviCourseOfStudiesBinding
import com.example.quizapp.databinding.RviMenuBinding
import com.example.quizapp.extensions.getColor
import com.example.quizapp.extensions.log
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCourseOfStudiesSelection : BindingListAdapter<CourseOfStudies, RviCourseOfStudiesBinding>(CourseOfStudies.DIFF_CALLBACK) {

    var onItemClicked: ((String) -> (Unit))? = null

    var selectionPredicate: ((CourseOfStudies) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviCourseOfStudiesBinding, vh: BindingListAdapterViewHolder) {
        binding.root.onClick {
            onItemClicked?.invoke(getItem(vh).id)
        }
    }

    override fun bindViews(binding: RviCourseOfStudiesBinding, item: CourseOfStudies, position: Int) {
        binding.apply {
            tvAbbreviation.text = item.abbreviation
            tvName.text = item.name

            if(selectionPredicate.invoke(item) && selectionColor != null){
                root.setCardBackgroundColor(selectionColor!!)
            } else {
                root.setCardBackgroundColor(getColor(R.color.transparent))
            }
        }
    }
}