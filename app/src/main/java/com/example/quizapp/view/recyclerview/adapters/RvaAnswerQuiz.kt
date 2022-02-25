package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviAnswerQuizBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaAnswerQuiz(
    private val isShowSolutionScreen: Boolean
) : BindingListAdapter<Answer, RviAnswerQuizBinding>(Answer.DIFF_CALLBACK) {

    companion object {
        private const val CHAR_A_INDEX = 65
    }

    var onItemClick: ((String) -> (Unit))? = null

    override fun initListeners(binding: RviAnswerQuizBinding, vh: BindingListAdapterViewHolder) {
        if (isShowSolutionScreen) return
        binding.root.onClick { onItemClick?.invoke(getItem(vh).id) }
    }

    override fun bindViews(binding: RviAnswerQuizBinding, item: Answer, position: Int) {
        binding.apply {
            tvAnswerText.text = item.answerText
            tvQuestionIndex.text = Char(position + CHAR_A_INDEX).toString()

            if (isShowSolutionScreen) {
                tvQuestionIndex.setBackgroundTintWithRes(if (item.isAnswerCorrect) R.color.hfuBrightGreen else R.color.red)
                isSelectedIndicator.isVisible = item.isAnswerSelected
            } else {
                tvQuestionIndex.setBackgroundTint(if (item.isAnswerSelected) getColor(R.color.hfuLightGreen) else getColor(defaultBackgroundColor))
                tvQuestionIndex.setTextColor(if (item.isAnswerSelected) getThemeColor(R.attr.invertedDominantTextColor) else getThemeColor(R.attr.defaultTextColor))
            }
        }
    }
}