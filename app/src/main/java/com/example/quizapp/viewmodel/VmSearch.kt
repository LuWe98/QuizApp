package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import androidx.paging.*
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.ktor.paging.MongoQuestionnairePagingSource
import com.example.quizapp.model.ktor.paging.EmptyPagingSource
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.room.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmSearch @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val fragmentSearchEventChannel = Channel<FragmentSearchEvent>()

    val fragmentSearchEventChannelFlow get() = fragmentSearchEventChannel.receiveAsFlow()

    private val searchQuery = MutableLiveData("")

    val filteredPagedData = searchQuery.switchMap { query ->
        if(query.isEmpty()){
            Pager(PagingConfig(pageSize = 0)) {  EmptyPagingSource<Int, MongoQuestionnaire>() }.liveData
        } else {
            Pager(config = PagingConfig(pageSize = PagingConfigValues.PAGE_SIZE, maxSize = PagingConfigValues.MAX_SIZE),
                pagingSourceFactory = { MongoQuestionnairePagingSource(backendRepository, query) }).liveData
        }
    }.cachedIn(viewModelScope).distinctUntilChanged()

    fun onSearchQueryChanged(query: String){
        searchQuery.value = query
    }


    fun onBackButtonClicked(){
        launch {
            fragmentSearchEventChannel.send(FragmentSearchEvent.NavigateBack)
        }
    }

    fun onFilterButtonClicked(){
        launch {
            fragmentSearchEventChannel.send(FragmentSearchEvent.NavigateToFilterScreen)
        }
    }



    sealed class FragmentSearchEvent {
        object NavigateBack: FragmentSearchEvent()
        object NavigateToFilterScreen: FragmentSearchEvent()
    }
}