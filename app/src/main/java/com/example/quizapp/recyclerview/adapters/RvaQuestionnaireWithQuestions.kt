package com.example.quizapp.recyclerview.adapters

import com.example.quizapp.databinding.RviQuestionnaireBinding
import com.example.quizapp.databinding.RviQuestionnaireNewBinding
import com.example.quizapp.extensions.setDrawableSize
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestions
import com.example.quizapp.recyclerview.impl.BindingPagingDataAdapter

class RvaQuestionnaireWithQuestions : BindingPagingDataAdapter<QuestionnaireWithQuestions, RviQuestionnaireNewBinding>(QuestionnaireWithQuestions.DIFF_CALLBACK) {

    var onItemClick : ((Questionnaire) -> (Unit))? = null

    var onItemLongClick: ((Questionnaire) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireNewBinding, vh: BindingPagingDataAdapterViewHolder) {
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

    override fun bindViews(binding: RviQuestionnaireNewBinding, item: QuestionnaireWithQuestions, position: Int) {
        binding.apply {
            item.questionnaire.let {
                questionnaireTitle.text = it.title
                courseOfStudiesName.text = it.faculty
                authorName.text = it.author
//                subjectName.text = it.subject
            }

            questionAmount.text = item.questionsAmount.toString()

//            subjectName.setDrawableSize(18)
            courseOfStudiesName.setDrawableSize(18)
            authorName.setDrawableSize(18)
            questionAmount.setDrawableSize(18)

            //progressIndicator.progress = item.completedQuestionsPercentage
            //checkMarkIcon.isVisible = progressIndicator.progress == 100
        }
    }
}