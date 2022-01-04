package com.example.quizapp.view.recyclerview.adapters

import android.annotation.SuppressLint
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionAddEditBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onLongClick
import com.example.quizapp.extensions.onTouch
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaAddEditQuestion : BindingListAdapter<QuestionWithAnswers, RviQuestionAddEditBinding>(QuestionWithAnswers.DIFF_CALLBACK){

    var onItemClick : ((QuestionWithAnswers) -> (Unit))? = null

    var onItemLongClicked : ((QuestionWithAnswers) -> (Unit))? = null

    var onDragHandleTouched: ((BindingListAdapterViewHolder) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionAddEditBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick { onItemClick?.invoke(getItem(vh)) }
            root.onLongClick { onItemLongClicked?.invoke(getItem(vh)) }
            dragHandle.onTouch { onDragHandleTouched?.invoke(vh) }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindViews(binding: RviQuestionAddEditBinding, item: QuestionWithAnswers, position: Int) {
        binding.apply {
            tvNumber.text = "${item.question.questionPosition + 1}"
            tvTitle.text = item.question.questionText
            ivQuestionType.setImageDrawable(if(item.question.isMultipleChoice) R.drawable.ic_check_circle else R.drawable.ic_radio_button)
        }
    }


//    btnTextExpand.onClick {
//        TransitionManager.beginDelayedTransition(root)
//        if(tvTitle.maxLines == Int.MAX_VALUE) {
//            tvTitle.maxLines = 4
//        } else {
//            tvTitle.maxLines = Int.MAX_VALUE
//        }
//    }
    //            tvTitle.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    tvTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                    val ellipsisCount = tvTitle.layout.getEllipsisCount(tvTitle.lineCount - 1)
//
//                    btnTextExpand.isVisible = ellipsisCount > 0
//                }
//            })
}