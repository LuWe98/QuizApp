package com.example.quizapp.view.recyclerview.adapters

import android.annotation.SuppressLint
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionAddEditBinding
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaQuestionAddEdit : BindingListAdapter<QuestionWithAnswers, RviQuestionAddEditBinding>(QuestionWithAnswers.DIFF_CALLBACK){

    var onItemClick : ((Int, QuestionWithAnswers) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionAddEditBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.setOnClickListener {
                onItemClick?.invoke(vh.bindingAdapterPosition, getItem(vh.bindingAdapterPosition))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindViews(binding: RviQuestionAddEditBinding, item: QuestionWithAnswers, position: Int) {
        binding.apply {
            tvNumber.text = "${position+1})"
            tvTitle.text = item.question.questionText
            ivQuestionType.setImageDrawable(if(item.question.isMultipleChoice) R.drawable.ic_check_circle else R.drawable.ic_radio_button)
        }
    }
}