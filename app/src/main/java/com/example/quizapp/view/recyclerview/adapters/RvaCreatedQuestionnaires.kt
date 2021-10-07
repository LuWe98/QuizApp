package com.example.quizapp.view.recyclerview.adapters

import android.content.res.ColorStateList
import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireCreatedBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.room.SyncStatus
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestions
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCreatedQuestionnaires : BindingListAdapter<QuestionnaireWithQuestions, RviQuestionnaireCreatedBinding>(QuestionnaireWithQuestions.DIFF_CALLBACK) {

    var onItemClick : ((String) -> (Unit))? = null

    var onItemLongClick: ((String) -> (Unit))? = null

    var onMoreOptionsClick : ((Questionnaire) -> (Unit))? = null

    var onSyncClick: ((String) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireCreatedBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onItemClick?.invoke(it.questionnaire.id)
                }
            }

            root.onLongClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onItemLongClick?.invoke(it.questionnaire.id)
                }
            }

            btnMoreOptions.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onMoreOptionsClick?.invoke(it.questionnaire)
                }
            }

            btnSync.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    if(it.questionnaire.syncStatus != SyncStatus.SYNCING){
                        onSyncClick?.invoke(it.questionnaire.id)
                    }
                }
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireCreatedBinding, item: QuestionnaireWithQuestions, position: Int) {
        binding.apply {
            questionnaireTitle.text = item.questionnaire.title
            val isSynced = item.questionnaire.syncStatus == SyncStatus.SYNCED
            btnSync.setImageDrawable(if(isSynced) R.drawable.ic_cloud_done else R.drawable.ic_cloud_upload)
            btnSync.backgroundTintList = ColorStateList.valueOf(if(isSynced) getThemeColor(R.attr.colorAccent) else getColor(R.color.unselectedColor))
            syncProgress.isVisible = item.questionnaire.syncStatus == SyncStatus.SYNCING
        }
    }
}