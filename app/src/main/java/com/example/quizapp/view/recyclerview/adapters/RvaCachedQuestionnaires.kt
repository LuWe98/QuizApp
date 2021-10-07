package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireBinding
import com.example.quizapp.extensions.context
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onLongClick
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestions
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCachedQuestionnaires : BindingListAdapter<QuestionnaireWithQuestions, RviQuestionnaireBinding>(QuestionnaireWithQuestions.DIFF_CALLBACK) {

    var onItemClick : ((String) -> (Unit))? = null

    var onItemLongClick: ((Questionnaire) -> (Unit))? = null

    var onMoreOptionsClicked : ((Questionnaire) -> (Unit))? = null


    override fun initListeners(binding: RviQuestionnaireBinding, vh: BindingListAdapterViewHolder) {
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

            btnMoreOptions.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onMoreOptionsClicked?.invoke(it.questionnaire)
                }
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireBinding, item: QuestionnaireWithQuestions, position: Int) {
        binding.apply {
//            item.questionnaire.let {
//                questionnaireTitle.text = it.title
//                tvCourseOfStudies.text = it.courseOfStudies
//                tvAuthor.text = it.authorInfo.userName
//            }
            tvTitle.text = item.questionnaire.title

            tvInfo.text = context.getString(R.string.test,
                item.questionnaire.authorInfo.userName,
                item.questionnaire.courseOfStudies,
                item.questionnaire.subject,
                item.questionsAmount.toString())

//            tvQuestionAmount.text = item.questionsAmount.toString()
//
//            tvAuthor.setDrawableSize(18)
//            tvQuestionAmount.setDrawableSize(18)
//            tvCourseOfStudies.setDrawableSize(18)

            //progressIndicator.progress = item.completedQuestionsPercentage
            //checkMarkIcon.isVisible = progressIndicator.progress == 100
        }
    }
}