package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviAnswerQuizBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.example.quizapp.viewmodel.VmQuiz

class RvaAnswerQuiz(
    private val vmQuiz: VmQuiz,
    private val isMultipleChoice: Boolean,
    private val isShowSolutionScreen: Boolean
) : BindingListAdapter<Answer, RviAnswerQuizBinding>(Answer.DIFF_CALLBACK) {

    var onItemClick: ((String, List<Answer>) -> (Unit))? = null

    override fun initListeners(binding: RviAnswerQuizBinding, vh: BindingListAdapterViewHolder) {
        if(isShowSolutionScreen) return

        binding.root.setOnClickListener {
            getItem(vh).let { answer ->
                onItemClick?.invoke(answer.id, currentList)
            }
        }
    }

    override fun bindViews(binding: RviAnswerQuizBinding, item: Answer, position: Int) {
        binding.apply {
            tvAnswerText.text = item.answerText
            
            ivSelectedIcon.apply {
                isVisible = item.isAnswerSelected
                setImageDrawable(if (isMultipleChoice) R.drawable.ic_check else R.drawable.ic_circle)
            }

            if (vmQuiz.shouldDisplaySolution || isShowSolutionScreen) {
                //tvAnswerText.isEnabled = item.isAnswerCorrect
                //tvAnswerText.setTextColorWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
                tvAnswerText.setTextColor(if (item.isAnswerCorrect) getColor(R.color.green) else getThemeColor(R.attr.colorControlNormal))
                ivSelectedIcon.setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
                ivRing.setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
            } else {
                tvAnswerText.isEnabled = true
                tvAnswerText.setTextColor(getThemeColor(R.attr.colorControlNormal))
                ivSelectedIcon.setDrawableTint(getThemeColor(R.attr.colorAccent))
                ivRing.setDrawableTint(if (item.isAnswerSelected) getThemeColor(R.attr.colorAccent) else getColor(R.color.unselectedColor))
            }
        }
    }
}