package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireCachedBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCachedQuestionnaires : BindingListAdapter<CompleteQuestionnaire, RviQuestionnaireCachedBinding>(CompleteQuestionnaire.DIFF_CALLBACK) {

    var onItemClick : ((String) -> (Unit))? = null

    var onItemLongClick: ((Questionnaire) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireCachedBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onItemClick?.invoke(it.questionnaire.id)
                }
            }

            root.onLongClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onItemLongClick?.invoke(it.questionnaire)
                }
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireCachedBinding, item: CompleteQuestionnaire, position: Int) {
        binding.apply {
            tvTitle.text = item.questionnaire.title
            tvAuthorDateAndQuestionAmount.text = context.getString(R.string.authorNameDateAndQuestionAmount,
                item.questionnaire.authorInfo.userName,
                item.questionnaire.timeStampAsDate,
                item.questionsAmount.toString())

            tvInfo.text = context.getString(R.string.cosAndSubject, item.courseOfStudies?.abbreviation, item.questionnaire.subject)

//            val answersPresent = item.allAnswers.isNotEmpty()
            progressIndicator.progress = item.answeredQuestionsPercentage

            item.areAllQuestionsCorrectlyAnswered.let {
                checkMarkIcon.isVisible = it
                progressIndicator.setIndicatorColor(if(it) getColor(R.color.green) else getThemeColor(R.attr.colorAccent))
            }
        }
    }
}