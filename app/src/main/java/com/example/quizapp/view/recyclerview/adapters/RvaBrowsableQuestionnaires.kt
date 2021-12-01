package com.example.quizapp.view.recyclerview.adapters

import android.content.res.ColorStateList
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireBrowseBinding
import com.example.quizapp.extensions.context
import com.example.quizapp.extensions.getColor
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onLongClick
import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter
import com.example.quizapp.viewmodel.VmSearch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RvaBrowsableQuestionnaires(
    private val vmSearch: VmSearch
) : BindingPagingDataAdapter<BrowsableQuestionnaire, RviQuestionnaireBrowseBinding>(BrowsableQuestionnaire.DIFF_CALLBACK) {

    var onDownloadClick: ((String) -> (Unit))? = null

    var onLongClicked: ((BrowsableQuestionnaire) -> (Unit))? = null

    var onItemClicked: ((BrowsableQuestionnaire) -> (Unit))? = null

    override fun initListeners(binding: RviQuestionnaireBrowseBinding, vh: BindingPagingDataAdapterViewHolder) {
        binding.apply {
            root.onLongClick {
                getItem(vh)?.let {
                    onLongClicked?.invoke(it)
                }
            }

            root.onClick {
                getItem(vh)?.let {
                    onItemClicked?.invoke(it)
                }
            }

            btnDownload.onClick {
                getItem(vh)?.let {
                    if(it.downloadStatus == DownloadStatus.NOT_DOWNLOADED){
                        onDownloadClick?.invoke(it.questionnaireId)
                    }
                }
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireBrowseBinding, item: BrowsableQuestionnaire, position: Int) {
        binding.apply {
            tvTitle.text = item.title
            tvDateAndQuestionAmount.text = context.getString(
                R.string.authorNameDateAndQuestionAmount,
                item.authorInfo.userName,
                item.timeStampAsDate,
                item.questionCount.toString()
            )

            vmSearch.viewModelScope.launch(IO) {
                val courseOfStudiesAbbreviations = vmSearch.getCourseOfStudiesNameWithIds(item.courseOfStudiesIds).reduceOrNull { acc, s -> "$acc, $s" } ?: ""

                withContext(Main){
                    tvInfo.text = context.getString(
                        R.string.cosAndSubject,
                        courseOfStudiesAbbreviations,
                        item.subject)
                }
            }

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