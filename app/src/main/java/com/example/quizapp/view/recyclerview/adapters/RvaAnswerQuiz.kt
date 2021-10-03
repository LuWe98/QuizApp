package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviAnswerQuizBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer

class RvaAnswerQuiz(
    private val vmQuiz: VmQuiz,
    private val vmQuizQuestionsContainer: VmQuizQuestionsContainer,
    private val isMultipleChoice: Boolean
) : BindingListAdapter<Answer, RviAnswerQuizBinding>(Answer.DIFF_CALLBACK) {

    var onItemClick: ((List<Answer>) -> (Unit))? = null

    override fun initListeners(binding: RviAnswerQuizBinding, vh: BindingListAdapterViewHolder) {
        binding.root.setOnClickListener {
            getItem(vh.bindingAdapterPosition).let { answer ->
                if (isMultipleChoice) {
                    handleMultipleChoiceClick(answer)
                } else if (!answer.isAnswerSelected) {
                    handleSingleChoiceClick(answer)
                }
            }
        }
    }

    private fun handleMultipleChoiceClick(answer: Answer) {
        answer.copy(isAnswerSelected = !answer.isAnswerSelected).apply {
            onItemClick?.invoke(mutableListOf(this))
        }
    }

    private fun handleSingleChoiceClick(answer: Answer) {
        answer.copy(isAnswerSelected = true).apply {
            currentList.firstOrNull { it.isAnswerSelected }?.copy(isAnswerSelected = false)?.let {
                onItemClick?.invoke(mutableListOf(this, it))
            } ?: onItemClick?.invoke(mutableListOf(this))
        }
    }

    override fun bindViews(binding: RviAnswerQuizBinding, item: Answer, position: Int) {
        binding.apply {
            tvAnswerText.text = item.answerText

            if (vmQuiz.shouldDisplaySolution || vmQuizQuestionsContainer.shouldDisplayQuestionSolution(item.questionId)) {
                tvAnswerText.setTextColorWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
                ivSelectedIcon.setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
                ivRing.setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
            } else {
                tvAnswerText.setTextColorWithRes(R.color.black)
                ivSelectedIcon.setDrawableTint(getThemeColor(R.attr.colorAccent))
                ivRing.setDrawableTint(if (item.isAnswerSelected) getThemeColor(R.attr.colorAccent) else getColor(R.color.unselectedColor))
            }

            ivSelectedIcon.apply {
                isVisible = item.isAnswerSelected
                setImageDrawable(if (isMultipleChoice) R.drawable.ic_check else R.drawable.ic_circle)
            }
        }
    }
}