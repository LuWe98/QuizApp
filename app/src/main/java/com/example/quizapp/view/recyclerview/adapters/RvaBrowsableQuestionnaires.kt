package com.example.quizapp.view.recyclerview.adapters

import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.R
import com.example.quizapp.databinding.RviQuestionnaireBrowseBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.databases.dto.MongoBrowsableQuestionnaire
import com.example.quizapp.view.recyclerview.impl.BindingPagingDataAdapter
import com.example.quizapp.viewmodel.VmSearch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class RvaBrowsableQuestionnaires(
    private val vmSearch: VmSearch
) : BindingPagingDataAdapter<MongoBrowsableQuestionnaire, RviQuestionnaireBrowseBinding>(MongoBrowsableQuestionnaire.DIFF_CALLBACK) {

    var onDownloadClick: ((String) -> (Unit))? = null

    var onLongClicked: ((MongoBrowsableQuestionnaire) -> (Unit))? = null

    var onItemClicked: ((MongoBrowsableQuestionnaire) -> (Unit))? = null

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
                        onDownloadClick?.invoke(it.id)
                    }
                }
            }
        }
    }

    override fun bindViews(binding: RviQuestionnaireBrowseBinding, item: MongoBrowsableQuestionnaire, position: Int) {
        binding.apply {
            tvTitle.text = item.title
            tvDateAndQuestionAmount.text = context.getString(
                R.string.cosAndSubject,
                item.authorInfo.userName,
                item.timeStampAsDate
            )

            btnDownload.setBackgroundTintWithRes(if(item.downloadStatus == DownloadStatus.DOWNLOADED) R.color.hfuBrightGreen else R.color.hfuLightGreen)
            btnDownload.setImageDrawable(if(item.downloadStatus == DownloadStatus.DOWNLOADED) R.drawable.ic_downloaded else R.drawable.ic_download)
            //btnDownload.setImageDrawable(null)


            vmSearch.viewModelScope.launch(IO) {
                val courseOfStudiesAbbreviations = vmSearch.getCourseOfStudiesNameWithIds(item.courseOfStudiesIds).reduceOrNull { acc, s -> "$acc, $s" } ?: ""

//                withContext(Main){
//                    tvInfo.text = context.getString(
//                        R.string.cosAndSubject,
//                        courseOfStudiesAbbreviations,
//                        item.subject)
//                }
            }

//            when(item.downloadStatus){
//                DownloadStatus.DOWNLOADED -> {
//                    progressIndicator.isVisible = false
//                    btnDownload.backgroundTintList = ColorStateList.valueOf(getColor(R.color.green))
//                }
//                DownloadStatus.DOWNLOADING -> {
//                    progressIndicator.isVisible = true
//                    btnDownload.backgroundTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
//                }
//                DownloadStatus.NOT_DOWNLOADED -> {
//                    progressIndicator.isVisible = false
//                    btnDownload.backgroundTintList = ColorStateList.valueOf(getColor(R.color.unselectedColor))
//                }
//            }
        }
    }

    fun changeItemDownloadStatus(questionnaireId: String, newStatus: DownloadStatus){
        snapshot().indexOfFirst { it?.id == questionnaireId }.let { index ->
            if(index == RecyclerView.NO_POSITION) return@let
            snapshot()[index]?.downloadStatus = newStatus
            notifyItemChanged(index)
        }
    }
}