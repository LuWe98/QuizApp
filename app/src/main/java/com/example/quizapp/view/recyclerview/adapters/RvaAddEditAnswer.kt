package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviAnswerAddEditBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.example.quizapp.viewmodel.VmAddEditQuestion

class RvaAddEditAnswer(
    private val vmEditQuestion: VmAddEditQuestion
) : BindingListAdapter<Answer, RviAnswerAddEditBinding>(Answer.DIFF_CALLBACK) {

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
            answerCorrectLine.setBackgroundColorWithRes(if (item.isAnswerCorrect) R.color.green else R.color.red)



//            btnCheck.setBackgroundTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
//            etAnswer.setTextColorWithRes(if (item.isAnswerCorrect) R.color.green else R.color.black)
//            ivSelectedIcon.apply {
//                isVisible = item.isAnswerCorrect
//                setImageDrawable(if (vmEditQuestion.isQuestionMultipleChoice) R.drawable.ic_check else R.drawable.ic_circle)
//                setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
//            }
        }
    }
}