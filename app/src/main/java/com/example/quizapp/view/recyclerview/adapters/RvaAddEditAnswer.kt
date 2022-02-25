package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviAnswerAddEditBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaAddEditAnswer: BindingListAdapter<Answer, RviAnswerAddEditBinding>(Answer.DIFF_CALLBACK) {

    var onItemClick: ((Answer) -> (Unit))? = null

    var onItemLongClicked : ((Answer) -> (Unit))? = null

    var onDragHandleTouched: ((BindingListAdapterViewHolder) -> (Unit))? = null

    override fun initListeners(binding: RviAnswerAddEditBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick { onItemClick?.invoke(getItem(vh)) }
            root.onLongClick { onItemLongClicked?.invoke(getItem(vh)) }
            dragHandle.onTouch { onDragHandleTouched?.invoke(vh) }
        }
    }

    override fun bindViews(binding: RviAnswerAddEditBinding, item: Answer, position: Int) {
        binding.apply {
            tvAnswerText.text = item.answerText
            tvQuestionIndex.text = Char(position + 65).toString()
            tvQuestionIndex.setBackgroundTintWithRes(if (item.isAnswerCorrect) R.color.hfuLightGreen else R.color.red)
        }
    }
}