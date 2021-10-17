package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.ktor.paging.UserPagingSource
import com.example.quizapp.model.room.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VmAdmin @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val searchQuery = MutableLiveData("")

    val filteredPagedData = Pager(config = PagingConfig(
        pageSize = PagingConfigValues.PAGE_SIZE,
        maxSize = PagingConfigValues.MAX_SIZE
    ), pagingSourceFactory = { UserPagingSource(backendRepository, "") }).liveData.cachedIn(viewModelScope).distinctUntilChanged()


//    val filteredPagedData = searchQuery.switchMap { query ->
//        if(query.isEmpty()){
//            Pager(PagingConfig(pageSize = 0)) {  EmptyPagingSource<Int, User>() }.liveData
//        } else {
//            Pager(config = PagingConfig(pageSize = PagingConfigValues.PAGE_SIZE, maxSize = PagingConfigValues.MAX_SIZE),
//                pagingSourceFactory = { UserPagingSource(backendRepository, query) }).liveData
//        }
//    }.cachedIn(viewModelScope).distinctUntilChanged()

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

}