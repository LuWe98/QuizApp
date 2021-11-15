package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviMenuBinding
import com.example.quizapp.extensions.getColor
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaFacultySelection : BindingListAdapter<Faculty, RviMenuBinding>(Faculty.DIFF_CALLBACK) {

    var onItemClicked: ((String) -> (Unit))? = null

    var selectionPredicate: ((Faculty) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviMenuBinding, vh: BindingListAdapterViewHolder) {
        binding.root.onClick {
            onItemClicked?.invoke(getItem(vh).id)
        }
    }

    override fun bindViews(binding: RviMenuBinding, item: Faculty, position: Int) {
        binding.apply {
            title.text = item.name

            if(selectionPredicate.invoke(item) && selectionColor != null){
                root.setCardBackgroundColor(selectionColor!!)
            } else {
                root.setCardBackgroundColor(getColor(R.color.transparent))
            }
        }
    }
}