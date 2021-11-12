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
            root.setOnClickListener {
                onItemClick?.invoke(vh.bindingAdapterPosition, getItem(vh.bindingAdapterPosition).question.id, root)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindViews(binding: RviQuestionQuizBinding, item: QuestionWithAnswers, position: Int) {
        binding.apply {
            tvNumber.text = "${position + 1}"
            tvTitle.text = item.question.questionText

            val backgroundTint: Int = if (item.isAnswered) {
                if (vmQuiz.shouldDisplaySolution) {
                    getColor(if (item.isAnsweredCorrectly) R.color.green else R.color.red)
                } else {
                    context.getThemeColor(R.attr.colorAccent)
                }
            } else {
                getColor(defaultBackgroundColor)
            }
            ivRing.setBackgroundTint(backgroundTint)
            tvNumber.setTextColor(if(item.isAnswered) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal))
        }
    }
}