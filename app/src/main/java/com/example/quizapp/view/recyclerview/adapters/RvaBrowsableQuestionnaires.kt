package com.example.quizapp.view.recyclerview.adapters

import android.content.res.ColorStateList
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireBrowseNewBinding
import com.example.quizapp.extensions.context
import com.example.quizapp.extensions.getColor
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.mongodb.documents.questionnaire.browsable.MongoBrowsableQuestionnaire
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter

class RvaBrowsableQuestionnaires : BindingPagingDataAdapter<MongoBrowsableQuestionnaire, RviQuestionnaireBrowseNewBinding>(MongoBrowsableQuestionnaire.DIFF_CALLBACK) {

    var onDownloadClick : ((String) -> (Unit))? = null

    var onMoreOptionsClicked : ((MongoBrowsableQuestionnaire) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireBrowseNewBinding, vh: BindingPagingDataAdapterViewHolder) {
        binding.apply {
            btnMoreOptions.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    onMoreOptionsClicked?.invoke(it)
                }
            }

            btnDownload.onClick {
                getItem(vh.bindingAdapterPosition)?.let {
                    if(it.downloadStatus == DownloadStatus.NOT_DOWNLOADED){
                        onDownloadClick?.invoke(it.questionnaireId)
                    }
                }
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireBrowseNewBinding, item: MongoBrowsableQuestionnaire, position: Int) {
        binding.apply {
            tvTitle.text = item.title
            tvInfo.text = context.getString(R.string.test, item.authorInfo.userName, item.courseOfStudies, item.subject, item.questionCount.toString())

            when(item.downloadStatus){
                DownloadStatus.DOWNLOADED -> {
                    progressIndicator.isVisible = false
                    btnDownload.backgroundTintList = ColorStateList.valueOf(getColor(R.color.green))
                }
                DownloadStatus.DOWNLOADING -> {
                    progressIndicator.isVisible = true
                    btnDownload.backgroundTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
                }
                DownloadStatus.NOT_DOWNLOADED -> {
                    progressIndicator.isVisible = false
                    btnDownload.backgroundTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
                }
            }
        }
    }

    fun changeItemDownloadStatus(questionnaireId: String, newStatus: DownloadStatus){
        snapshot().indexOfFirst { it?.questionnaireId == questionnaireId }.let { index ->
            if(index == RecyclerView.NO_POSITION) return@let
            snapshot()[index]?.downloadStatus = newStatus
            notifyItemChanged(index)
        }
    }
}