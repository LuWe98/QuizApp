package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.MongoQuestionnairePagingSource
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.ktor.responses.GetQuestionnaireResponse.GetQuestionnaireResponseType
import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.viewmodel.VmSearch.FragmentSearchEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmSearch @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val fragmentSearchEventChannel = Channel<FragmentSearchEvent>()

    val fragmentSearchEventChannelFlow get() = fragmentSearchEventChannel.receiveAsFlow()

    private val searchQuery = MutableStateFlow("")

    val filteredPagedData = searchQuery.flatMapLatest { query ->
        Pager(
            config = PagingConfig(pageSize = PagingConfigValues.PAGE_SIZE, maxSize = PagingConfigValues.MAX_SIZE),
            pagingSourceFactory = {
                MongoQuestionnairePagingSource(backendRepository, localRepository, query)
            }).flow.cachedIn(viewModelScope)
    }

    suspend fun getCourseOfStudiesNameWithId(courseOfStudiesId: String) = localRepository.getCourseOfStudiesNameWithId(courseOfStudiesId)

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onBackButtonClicked() = launch {
        fragmentSearchEventChannel.send(NavigateBack)
    }

    fun onFilterButtonClicked() = launch {
        fragmentSearchEventChannel.send(NavigateToFilterScreen)
    }


    fun onItemDownLoadButtonClicked(questionnaireId: String) = launch(IO) {
        fragmentSearchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DownloadStatus.DOWNLOADING))

        runCatching {
            backendRepository.downloadQuestionnaire(questionnaireId)
        }.onSuccess { response ->
            when(response.responseType){
                GetQuestionnaireResponseType.SUCCESSFUL -> {
                    DataMapper.mapMongoQuestionnaireToRoomCompleteQuestionnaire(response.mongoQuestionnaire!!).let {
                        localRepository.insertCompleteQuestionnaire(it)
                        localRepository.deleteLocallyDeletedQuestionnaireWith(it.questionnaire.id)
                        fragmentSearchEventChannel.send(ShowMessageSnackBar(R.string.questionnaireDownloaded))
                        fragmentSearchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DownloadStatus.DOWNLOADED))
                    }
                }
                GetQuestionnaireResponseType.QUESTIONNAIRE_NOT_FOUND -> {
                    fragmentSearchEventChannel.send(ShowMessageSnackBar(R.string.errorQuestionnaireCouldNotBeFound))
                    fragmentSearchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DownloadStatus.NOT_DOWNLOADED))
                }
            }
        }.onFailure {
            fragmentSearchEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDownloadQuestionnaire))
            fragmentSearchEventChannel.send(ChangeItemDownloadStatusEvent(questionnaireId, DownloadStatus.NOT_DOWNLOADED))
        }
    }

    sealed class FragmentSearchEvent {
        object NavigateBack : FragmentSearchEvent()
        object NavigateToFilterScreen : FragmentSearchEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : FragmentSearchEvent()
        class ChangeItemDownloadStatusEvent(val questionnaireId: String, val downloadStatus: DownloadStatus): FragmentSearchEvent()
    }
}