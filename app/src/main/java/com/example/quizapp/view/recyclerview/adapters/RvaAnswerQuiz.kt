package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviAnswerQuizBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaAnswerQuiz(
    private val isShowSolutionScreen: Boolean
) : BindingListAdapter<Answer, RviAnswerQuizBinding>(Answer.DIFF_CALLBACK) {

    var onItemClick: ((String) -> (Unit))? = null

    override fun initListeners(binding: RviAnswerQuizBinding, vh: BindingListAdapterViewHolder) {
        if(isShowSolutionScreen) return
        binding.root.onClick { onItemClick?.invoke(getItem(vh).id) }
    }

    override fun bindViews(binding: RviAnswerQuizBinding, item: Answer, position: Int) {
        binding.apply {
            tvAnswerText.text = item.answerText
            tvQuestionIndex.text = Char(position + 65).toString()

            if (isShowSolutionScreen) {
                tvQuestionIndex.setBackgroundTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.red)

                //answerCorrectLine.setBackgroundColorWithRes(if (item.isAnswerCorrect) R.color.green else R.color.red)
            } else {
                tvQuestionIndex.setBackgroundTint(if (item.isAnswerSelected) getThemeColor(R.attr.colorPrimary) else getColor(defaultBackgroundColor))
                tvQuestionIndex.setTextColor(if (item.isAnswerSelected) getThemeColor(R.attr.invertedDominantTextColor) else getThemeColor(R.attr.defaultTextColor))
                tvAnswerText.setTextColor(if (item.isAnswerSelected) getThemeColor(R.attr.colorPrimary) else getThemeColor(R.attr.defaultTextColor))

                //tvQuestionIndex.setBackgroundTint(if (item.isAnswerSelected) getThemeColor(R.attr.colorPrimary) else getThemeColor(R.attr.colorOnPrimary))
            }


//            ivSelectedIcon.apply {
//                isVisible = item.isAnswerSelected
//                setImageDrawable(if (isMultipleChoice) R.drawable.ic_check else R.drawable.ic_circle)
//            }
//
//            if (isShowSolutionScreen) {
//                tvAnswerText.setTextColor(if (item.isAnswerCorrect) getColor(R.color.green) else getThemeColor(R.attr.colorControlNormal))
//                ivSelectedIcon.setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
//                ivRing.setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
//            } else {
//                tvAnswerText.setTextColor(getThemeColor(R.attr.colorControlNormal))
//                ivSelectedIcon.setDrawableTint(getThemeColor(R.attr.colorAccent))
//                ivRing.setDrawableTint(if (item.isAnswerSelected) getThemeColor(R.attr.colorAccent) else getColor(R.color.unselectedColor))
//            }
        }
    }
}