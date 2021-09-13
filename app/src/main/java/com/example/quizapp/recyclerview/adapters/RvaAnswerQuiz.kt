package com.example.quizapp.recyclerview.adapters

import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviAnswerBinding
import com.example.quizapp.extensions.getThemeColor
import com.example.quizapp.extensions.setDrawableTint
import com.example.quizapp.extensions.setTextColorWithRes
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.recyclerview.impl.BindingListAdapter
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer

class RvaAnswerQuiz(
    private val vmQuiz: VmQuiz,
    private val vmQuizQuestionsContainer: VmQuizQuestionsContainer,
    private val isMultipleChoice: Boolean
) : BindingListAdapter<Answer, RviAnswerBinding>(Answer.DIFF_CALLBACK) {

    var onItemClick: ((List<Answer>) -> (Unit))? = null

    override fun initListeners(binding: RviAnswerBinding, vh: BindingListAdapterViewHolder) {
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

    override fun bindViews(binding: RviAnswerBinding, item: Answer, position: Int) {
        binding.apply {
            answerText.text = item.text

            if (vmQuiz.shouldDisplaySolution || vmQuizQuestionsContainer.isQuestionIdInsideShouldDisplayList(item.questionId)) {
                answerText.setTextColorWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
            } else {
                answerText.setTextColorWithRes(R.color.black)
            }

            selectionButtonRing.setDrawableTint(
                if (item.isAnswerSelected) root.context.getThemeColor(R.attr.colorAccent)
                else ContextCompat.getColor(root.context, R.color.unselectedColor)
            )

            checkIcon.apply {
                isVisible = item.isAnswerSelected
                setImageDrawable(AppCompatResources.getDrawable(context, if (isMultipleChoice) R.drawable.ic_check else R.drawable.ic_circle))
            }
        }
    }
}