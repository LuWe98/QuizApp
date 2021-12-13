package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviAnswerAddEditNewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.example.quizapp.viewmodel.VmAddEditQuestion

class RvaAddEditAnswer(
    private val vmEditQuestion: VmAddEditQuestion
) : BindingListAdapter<Answer, RviAnswerAddEditNewBinding>(Answer.DIFF_CALLBACK) {

    var onItemClick: ((Answer) -> (Unit))? = null

    var onDeleteButtonClick: ((Answer) -> (Unit))? = null

    var onCheckButtonClicked: ((Answer) -> (Unit))? = null

    override fun initListeners(binding: RviAnswerAddEditNewBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick { onItemClick?.invoke(getItem(vh)) }
            btnDelete.onClick { onDeleteButtonClick?.invoke(getItem(vh)) }
            btnCheck.onClick { onCheckButtonClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviAnswerAddEditNewBinding, item: Answer, position: Int) {
        binding.apply {
            etAnswer.text = item.answerText
            etAnswer.setTextColorWithRes(if (item.isAnswerCorrect) R.color.green else R.color.black)
            btnCheck.setBackgroundTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)

            ivSelectedIcon.apply {
                isVisible = item.isAnswerCorrect
                setImageDrawable(if (vmEditQuestion.isQuestionMultipleChoice) R.drawable.ic_check else R.drawable.ic_circle)
                setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
            }
        }
    }
}