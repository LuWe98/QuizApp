package com.example.quizapp.view.recyclerview.adapters

import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.room.entities.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter

class RvaHomeQuestionnaires : BindingListAdapter<CompleteQuestionnaire, RviQuestionnaireBinding>(CompleteQuestionnaire.DIFF_CALLBACK) {

    var onItemClick: ((CompleteQuestionnaire) -> (Unit))? = null

    var onItemLongClick: ((Questionnaire) -> (Unit))? = null

    var onPlayButtonClick: ((CompleteQuestionnaire) -> (Unit))? = null

    var onSyncClick: ((String) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick {
                getItem(vh).let {
                    onItemClick?.invoke(it)
                }
            }

            root.onLongClick {
                getItem(vh).let {
                    onItemLongClick?.invoke(it.questionnaire)
                }
            }

            btnPlay.onClick {
                getItem(vh).let {
                    onPlayButtonClick?.invoke(it)
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

    override fun bindViews(binding: RviQuestionnaireBinding, item: CompleteQuestionnaire, position: Int) {
        binding.apply {
            tvTitle.text = item.questionnaire.title
            tvDateAndQuestionAmount.text = context.getString(
                R.string.cosAndSubject,
                item.questionnaire.authorInfo.userName,
                item.questionnaire.timeStampAsDate
            )

            (item.areAllQuestionsCorrectlyAnswered && item.hasQuestions).let { isCompleted ->
                btnPlay.setImageDrawable(if(isCompleted) R.drawable.ic_done_all else if(item.areAllQuestionsAnswered) R.drawable.ic_done else R.drawable.ic_play_arrow)
                btnPlay.setBackgroundTintWithRes(if(isCompleted) R.color.hfuBrightGreen else R.color.hfuLightGreen)
            }

            //            tvDateAndQuestionAmount.text = context.getString(
//                R.string.authorNameDateAndQuestionAmount,
//                item.questionnaire.authorInfo.userName,
//                item.questionnaire.timeStampAsDate,
//                item.questionsAmount.toString()
//            )
//            tvInfo.text = context.getString(R.string.cosAndSubject,
//                item.courseOfStudiesAbbreviations,
//                item.questionnaire.subject)
//            syncProgress.isVisible = item.questionnaire.syncStatus == SyncStatus.SYNCING

//            (item.questionnaire.syncStatus == SyncStatus.SYNCED).let { isSynced ->
//                btnSync.setImageDrawable(if (isSynced) R.drawable.ic_cloud_done else R.drawable.ic_cloud_upload)
//                btnSync.setDrawableTintWithRes((if(isSynced) R.color.green else R.color.unselectedColor))
//            }
        }
    }
}