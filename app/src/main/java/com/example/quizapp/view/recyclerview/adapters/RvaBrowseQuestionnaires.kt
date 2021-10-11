package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireBrowseBinding
import com.example.quizapp.databinding.RviQuestionnaireBrowseNew2Binding
import com.example.quizapp.databinding.RviQuestionnaireBrowseNewBinding
import com.example.quizapp.extensions.context
import com.example.quizapp.extensions.getString
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setDrawableSize
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter

class RvaBrowseQuestionnaires : BindingPagingDataAdapter<MongoQuestionnaire, RviQuestionnaireBrowseNewBinding>(MongoQuestionnaire.DIFF_CALLBACK) {

    var onDownloadClick : ((MongoQuestionnaire) -> (Unit))? = null

    var onMoreOptionsClicked : ((MongoQuestionnaire) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireBrowseNewBinding, vh: BindingPagingDataAdapterViewHolder) {
        binding.apply {
            btnMoreOptions.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onMoreOptionsClicked?.invoke(it)
                }
            }

            btnDownload.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    progressIndicator.isVisible = true
                    onDownloadClick?.invoke(it)
                }
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireBrowseNewBinding, item: MongoQuestionnaire, position: Int) {
        binding.apply {
            tvTitle.text = item.title
            tvInfo.text = context.getString(R.string.test, item.authorInfo.userName, item.courseOfStudies, item.subject, item.questions.size.toString())
//
//            tvAuthor.text = item.authorInfo.userName
//            tvCourseOfStudies.text = item.courseOfStudies
//            tvSubject.text = item.subject
//            tvQuestionAmount.text = item.questions.size.toString()
//
//            tvAuthor.setDrawableSize(14)
//            tvCourseOfStudies.setDrawableSize(14)
//            tvSubject.setDrawableSize(14)
//            tvQuestionAmount.setDrawableSize(14)
        }
    }
}