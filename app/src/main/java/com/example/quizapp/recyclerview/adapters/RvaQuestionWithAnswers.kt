package com.example.quizapp.recyclerview.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionBinding
import com.example.quizapp.extensions.getThemeColor
import com.example.quizapp.extensions.setDrawableTint
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.junctions.QuestionWithAnswers
import com.example.quizapp.recyclerview.impl.BindingListAdapter
import com.example.quizapp.viewmodel.VmQuiz

class RvaQuestionWithAnswers(
    private val vmQuiz: VmQuiz
) : BindingListAdapter<QuestionWithAnswers, RviQuestionBinding>(QuestionWithAnswers.DIFF_CALLBACK) {

    var onItemClick: ((Int, Long, CardView) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionBinding, vh: BindingListAdapterViewHolder) {
        binding.root.setOnClickListener {
            onItemClick?.invoke(vh.bindingAdapterPosition, getItem(vh.bindingAdapterPosition).question.id, binding.root)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindViews(binding: RviQuestionBinding, item: QuestionWithAnswers, position: Int) {
        binding.apply {
            questionNumberText.text = "${position + 1})"
            questionTitle.text = item.question.text

            val tint: Int

            if (item.isAnswered) {
                checkMarkIcon.isVisible = vmQuiz.shouldDisplaySolution
                if (vmQuiz.shouldDisplaySolution) {
                    val drawableRes: Int
                    if (item.isAnsweredCorrectly) {
                        drawableRes = R.drawable.ic_check
                        tint = ContextCompat.getColor(root.context, R.color.green)
                    } else {
                        drawableRes = R.drawable.ic_cross
                        tint = ContextCompat.getColor(root.context, R.color.red)
                    }
                    checkMarkIcon.setImageDrawable(drawableRes)
                    checkMarkIcon.setDrawableTint(tint)
                } else {
                    tint = root.context.getThemeColor(R.attr.colorAccent)
                }
            } else {
                checkMarkIcon.isVisible = false
                tint = ContextCompat.getColor(root.context, R.color.unselectedColor)
            }
            progressIndicator.setDrawableTint(tint)
        }
    }
}