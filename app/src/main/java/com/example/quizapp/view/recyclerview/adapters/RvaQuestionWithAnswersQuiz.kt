package com.example.quizapp.view.recyclerview.adapters

import android.annotation.SuppressLint
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionQuizBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.example.quizapp.viewmodel.VmQuiz

class RvaQuestionWithAnswersQuiz(
    private val vmQuiz: VmQuiz
) : BindingListAdapter<QuestionWithAnswers, RviQuestionQuizBinding>(QuestionWithAnswers.DIFF_CALLBACK) {

    var onItemClick: ((Int, Long, CardView) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionQuizBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.setOnClickListener {
                onItemClick?.invoke(vh.bindingAdapterPosition, getItem(vh.bindingAdapterPosition).question.id, root)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindViews(binding: RviQuestionQuizBinding, item: QuestionWithAnswers, position: Int) {
        binding.apply {
            tvNumber.text = "${position + 1})"
            tvTitle.text = item.question.text

            val tint: Int

            if (item.isAnswered) {
                ivResultIcon.isVisible = vmQuiz.shouldDisplaySolution
                if (vmQuiz.shouldDisplaySolution) {
                    val drawableRes: Int
                    if (item.isAnsweredCorrectly) {
                        drawableRes = R.drawable.ic_check
                        tint = getColor(R.color.green)
                    } else {
                        drawableRes = R.drawable.ic_cross
                        tint = getColor(R.color.red)
                    }
                    ivResultIcon.setImageDrawable(drawableRes)
                    ivResultIcon.setDrawableTint(tint)
                } else {
                    tint = context.getThemeColor(R.attr.colorAccent)
                }
            } else {
                ivResultIcon.isVisible = false
                tint = getColor(R.color.unselectedColor)
            }
            ivRing.setDrawableTint(tint)
        }
    }
}