package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireCreatedNewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaCreatedQuestionnaires : BindingListAdapter<CompleteQuestionnaire, RviQuestionnaireCreatedNewBinding>(CompleteQuestionnaire.DIFF_CALLBACK) {

    var onItemClick: ((String) -> (Unit))? = null

    var onItemLongClick: ((Questionnaire) -> (Unit))? = null

    var onSyncClick: ((String) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireCreatedNewBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick {
                getItem(vh).let {
                    onItemClick?.invoke(it.questionnaire.id)
                }
            }

            root.onLongClick {
                getItem(vh).let {
                    onItemLongClick?.invoke(it.questionnaire)
                }
            }

            btnSync.onClick {
                getItem(vh).let {
                    if (it.questionnaire.syncStatus == SyncStatus.UNSYNCED) {
                        onSyncClick?.invoke(it.questionnaire.id)
                    }
                }
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireCreatedNewBinding, item: CompleteQuestionnaire, position: Int) {
        binding.apply {
            tvTitle.text = item.questionnaire.title
            tvDateAndQuestionAmount.text = context.getString(
                R.string.authorNameDateAndQuestionAmount,
                item.questionnaire.authorInfo.userName,
                item.questionnaire.timeStampAsDate,
                item.questionsAmount.toString()
            )
            tvInfo.text = context.getString(R.string.cosAndSubject,
                item.questionnaire.courseOfStudies,
                item.questionnaire.subject)

            syncProgress.isVisible = item.questionnaire.syncStatus == SyncStatus.SYNCING

            (item.questionnaire.syncStatus == SyncStatus.SYNCED).let { isSynced ->
                btnSync.setImageDrawable(if (isSynced) R.drawable.ic_cloud_done else R.drawable.ic_cloud_upload)
                btnSync.setDrawableTintWithRes((if(isSynced) R.color.green else R.color.unselectedColor))
            }

            progressIndicator.progress = item.answeredQuestionsPercentage

            item.areAllQuestionsCorrectlyAnswered.let {
                checkMarkIcon.isVisible = it
                progressIndicator.setIndicatorColor(if(it) getColor(R.color.green) else getThemeColor(R.attr.colorAccent))
            }
        }
    }
}