package com.example.quizapp.view.recyclerview.adapters

import android.annotation.SuppressLint
import android.transition.TransitionManager
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionAddEditBinding
import com.example.quizapp.databinding.RviQuestionAddEditNewBinding
import com.example.quizapp.extensions.log
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import kotlin.random.Random

class RvaQuestionAddEdit : BindingListAdapter<QuestionWithAnswers, RviQuestionAddEditNewBinding>(QuestionWithAnswers.DIFF_CALLBACK){

    var onItemClick : ((Int, QuestionWithAnswers) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionAddEditNewBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick {
                onItemClick?.invoke(vh.bindingAdapterPosition, getItem(vh))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindViews(binding: RviQuestionAddEditNewBinding, item: QuestionWithAnswers, position: Int) {
        binding.apply {
            tvNumber.text = "${position + 1}"
            var test = ""
            repeat(Random.nextInt(12) +1){
                test += item.question.questionText + " "
            }
            tvTitle.text = test

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