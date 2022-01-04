package com.example.quizapp.view.recyclerview.adapters

import android.annotation.SuppressLint
import androidx.cardview.widget.CardView
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionQuizBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.example.quizapp.viewmodel.VmQuiz

class RvaQuestionQuiz(
    private val vmQuiz: VmQuiz
) : BindingListAdapter<QuestionWithAnswers, RviQuestionQuizBinding>(QuestionWithAnswers.DIFF_CALLBACK) {

    var onItemClick: ((Int, String, CardView) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionQuizBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick {
                onItemClick?.invoke(vh.bindingAdapterPosition, getItem(vh).question.id, root)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindViews(binding: RviQuestionQuizBinding, item: QuestionWithAnswers, position: Int) {
        binding.apply {
            tvNumber.text = "${item.question.questionPosition + 1}"
            tvTitle.text = item.question.questionText

            val backgroundTint = if (item.isAnswered) {
                if (vmQuiz.areAllQuestionsAnswered) {
                    getColor(if (item.isAnsweredCorrectly) R.color.green else R.color.red)
                } else {
                    context.getThemeColor(R.attr.colorAccent)
                }
            } else {
                getThemeColor(R.attr.colorControlNormal)
            }

            ivQuestionType.setDrawableTint(backgroundTint)
            ivQuestionType.setImageDrawable(if(item.question.isMultipleChoice) R.drawable.ic_check_circle else R.drawable.ic_radio_button)

//            view.setBackgroundTint(backgroundTint)
//            val iconTextTint = if(item.isAnswered) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)
//            tvNumber.setTextColor(iconTextTint)
        }
    }
}