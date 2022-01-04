package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviCourseOfStudiesSelectionBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCourseOfStudiesSelection : BindingListAdapter<CourseOfStudies, RviCourseOfStudiesSelectionBinding>(CourseOfStudies.DIFF_CALLBACK) {

    var onItemClicked: ((String) -> (Unit))? = null

    var selectionPredicate: ((CourseOfStudies) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviCourseOfStudiesSelectionBinding, vh: BindingListAdapterViewHolder) {
        binding.root.onClick {
            onItemClicked?.invoke(getItem(vh).id)
        }
    }

    override fun bindViews(binding: RviCourseOfStudiesSelectionBinding, item: CourseOfStudies, position: Int) {
        binding.apply {
            tvAbbreviation.text = item.abbreviation
            tvName.text = item.name
            checkBox.isChecked = selectionPredicate.invoke(item)
        }
    }
}