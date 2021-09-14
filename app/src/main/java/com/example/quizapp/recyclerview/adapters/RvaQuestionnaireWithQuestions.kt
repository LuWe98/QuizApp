package com.example.quizapp.recyclerview.adapters

import com.example.quizapp.databinding.RviQuestionnaireBinding
import com.example.quizapp.extensions.setDrawableSize
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestions
import com.example.quizapp.recyclerview.impl.BindingPagingDataAdapter

class RvaQuestionnaireWithQuestions : BindingPagingDataAdapter<QuestionnaireWithQuestions, RviQuestionnaireBinding>(QuestionnaireWithQuestions.DIFF_CALLBACK) {

    var onItemClick : ((Questionnaire) -> (Unit))? = null

    var onItemLongClick: ((Questionnaire) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireBinding, vh: BindingPagingDataAdapterViewHolder) {
        binding.apply {
            root.setOnClickListener {
                getItem(vh.bindingAdapterPosition)?.let {
                    onItemClick?.invoke(it.questionnaire)
                }
            }

            root.setOnLongClickListener {
                getItem(vh.bindingAdapterPosition)?.let {
                    onItemLongClick?.invoke(it.questionnaire)
                }
                return@setOnLongClickListener true
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireBinding, item: QuestionnaireWithQuestions, position: Int) {
        binding.apply {
            item.questionnaire.let {
                questionnaireTitle.text = it.title
                facultyName.text = it.faculty
                authorName.text = it.author
            }

            questionAmount.text = item.questionsAmount.toString()

            facultyName.setDrawableSize(15)
            authorName.setDrawableSize(15)
            questionAmount.setDrawableSize(15)

            //progressIndicator.progress = item.completedQuestionsPercentage
            //checkMarkIcon.isVisible = progressIndicator.progress == 100
        }
    }
}