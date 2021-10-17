package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireCachedBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCachedQuestionnaires : BindingListAdapter<CompleteQuestionnaireJunction, RviQuestionnaireCachedBinding>(CompleteQuestionnaireJunction.DIFF_CALLBACK) {

    var onItemClick : ((String) -> (Unit))? = null

    var onItemLongClick: ((String, String) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireCachedBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onItemClick?.invoke(it.questionnaire.id)
                }
            }

            root.onLongClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onItemLongClick?.invoke(it.questionnaire.authorInfo.userId, it.questionnaire.id)
                }
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireCachedBinding, item: CompleteQuestionnaireJunction, position: Int) {
        binding.apply {
            tvTitle.text = item.questionnaire.title
            tvAuthorDateAndQuestionAmount.text = context.getString(R.string.authorNameDateAndQuestionAmount,
                item.questionnaire.authorInfo.userName,
                item.questionnaire.timeStampAsDate,
                item.questionsAmount.toString())

            tvInfo.text = context.getString(R.string.cosAndSubject, item.questionnaire.courseOfStudies, item.questionnaire.subject)

//            val answersPresent = item.allAnswers.isNotEmpty()
            progressIndicator.progress = item.answeredQuestionsPercentage

            item.areAllQuestionsCorrectlyAnswered.let {
                checkMarkIcon.isVisible = it
                progressIndicator.setIndicatorColor(if(it) getColor(R.color.green) else getThemeColor(R.attr.colorAccent))
            }
        }
    }
}