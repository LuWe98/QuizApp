package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import androidx.paging.*
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.mongodb.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.mongodb.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.ktor.paging.MongoQuestionnairePagingSource
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.ktor.responses.InsertFilledQuestionnaireResponse.*
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.sync.LocallyDownloadedQuestionnaire
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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

    private val searchQuery = MutableLiveData("")

    val filteredPagedData = searchQuery.switchMap { query ->
        Pager(config = PagingConfig(pageSize = PagingConfigValues.PAGE_SIZE, maxSize = PagingConfigValues.MAX_SIZE),
            pagingSourceFactory = { MongoQuestionnairePagingSource(backendRepository, query) }).liveData
    }.cachedIn(viewModelScope).distinctUntilChanged()

//
//    val filteredPagedData = searchQuery.switchMap { query ->
//        if(query.isEmpty()){
//            Pager(PagingConfig(pageSize = 0)) {  EmptyPagingSource<Int, MongoQuestionnaire>() }.liveData
//        } else {
//            Pager(config = PagingConfig(pageSize = PagingConfigValues.PAGE_SIZE, maxSize = PagingConfigValues.MAX_SIZE),
//                pagingSourceFactory = { MongoQuestionnairePagingSource(backendRepository, query) }).liveData
//        }
//    }.cachedIn(viewModelScope).distinctUntilChanged()


    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onBackButtonClicked() {
        launch {
            fragmentSearchEventChannel.send(FragmentSearchEvent.NavigateBack)
        }
    }

    fun onFilterButtonClicked() {
        launch {
            fragmentSearchEventChannel.send(FragmentSearchEvent.NavigateToFilterScreen)
        }
    }




    //TODO -> Wenn man auf ein item draufdrückt
    fun onItemDownLoadButtonClicked(mongoQuestionnaire: MongoQuestionnaire) = launch(IO) {
        DataMapper.mapMongoObjectToSqlEntities(mongoQuestionnaire).let {
            localRepository.insertCompleteQuestionnaire(it)
            uploadEmptyFilledQuestionnaire(it.questionnaire.id)
        }
    }

    private fun uploadEmptyFilledQuestionnaire(questionnaireId: String) = applicationScope.launch(IO) {
        val response = try {
            backendRepository.insertEmptyFilledQuestionnaire(
                MongoFilledQuestionnaire(
                    questionnaireId = questionnaireId,
                    userId = preferencesRepository.userInfoFlow.first().id
                )
            )
        } catch (e: Exception) {
            localRepository.insert(LocallyDownloadedQuestionnaire(questionnaireId))
            null
        }

        when (response?.responseType) {
            InsertFilledQuestionnaireResponseType.INSERTED -> {
            }
            InsertFilledQuestionnaireResponseType.ERROR -> {
            }
            InsertFilledQuestionnaireResponseType.EMPTY_INSERTION_SKIPPED -> {
            }
            InsertFilledQuestionnaireResponseType.QUESTIONNAIRE_DOES_NOT_EXIST_ANYMORE -> {
            }
            null -> log("Empty one inserted!")
        }
    }

    sealed class FragmentSearchEvent {
        object NavigateBack : FragmentSearchEvent()
        object NavigateToFilterScreen : FragmentSearchEvent()
    }
}