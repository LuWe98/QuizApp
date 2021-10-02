package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.databinding.RviQuestionnaireBinding
import com.example.quizapp.extensions.setDrawableSize
import com.example.quizapp.model.realm.MongoQuestionnaire
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCreated : BindingListAdapter<MongoQuestionnaire, RviQuestionnaireBinding>(MongoQuestionnaire.DIFF_CALLBACK) {

    override fun initListeners(binding: RviQuestionnaireBinding, vh: BindingListAdapterViewHolder) {

    }

    override fun bindViews(binding: RviQuestionnaireBinding, item: MongoQuestionnaire, position: Int) {

        binding.apply {
            questionnaireTitle.text = item.title
            authorName.text = item.authorUserName
            questionAmount.text = item.questions.size.toString()
            courseOfStudiesName.text = "WIB"

            courseOfStudiesName.setDrawableSize(18)
            authorName.setDrawableSize(18)
            questionAmount.setDrawableSize(18)
        }
    }
}