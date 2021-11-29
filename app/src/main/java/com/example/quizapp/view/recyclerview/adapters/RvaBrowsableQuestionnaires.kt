package com.example.quizapp.view.recyclerview.adapters

import android.content.res.ColorStateList
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireBrowseNewBinding
import com.example.quizapp.extensions.context
import com.example.quizapp.extensions.getColor
import com.example.quizapp.extensions.onClick
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
) : BindingPagingDataAdapter<BrowsableQuestionnaire, RviQuestionnaireBrowseNewBinding>(BrowsableQuestionnaire.DIFF_CALLBACK) {

    var onDownloadClick : ((String) -> (Unit))? = null

    var onMoreOptionsClicked : ((BrowsableQuestionnaire) -> (Unit))? = null

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

    override fun bindViews(binding: RviQuestionnaireBrowseNewBinding, item: BrowsableQuestionnaire, position: Int) {
        binding.apply {
            tvTitle.text = item.title

            vmSearch.viewModelScope.launch(IO) {
                val courseOfStudiesAbbreviations = vmSearch.getCourseOfStudiesNameWithIds(item.courseOfStudiesIds).reduce { acc, s -> "$acc, $s" }

                withContext(Main){
                    tvInfo.text = context.getString(R.string.test, item.authorInfo.userName, courseOfStudiesAbbreviations, item.subject, item.questionCount.toString())
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