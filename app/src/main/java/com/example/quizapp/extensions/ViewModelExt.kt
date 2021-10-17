package com.example.quizapp.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object PagingConstants {
    private const val PAGING_PAGE_SIZE : Int = 20
    private const val PAGING_PREFETCH_DISTANCE = PAGING_PAGE_SIZE * 2
    private const val PAGING_INITIAL_LOAD_SIZE = PAGING_PAGE_SIZE * 3
    private const val PAGING_MAX_SIZE = PAGING_INITIAL_LOAD_SIZE * 5

    val PAGING_CONFIG : PagingConfig = PagingConfig(
        pageSize = PAGING_PAGE_SIZE,
        prefetchDistance = PAGING_PREFETCH_DISTANCE,
        initialLoadSize = PAGING_INITIAL_LOAD_SIZE,
        maxSize = PAGING_MAX_SIZE
    )
}

fun <T : Any> ViewModel.getPagingDataAsLiveData(dataSource : DataSource.Factory<Int, T>) =
    Pager(config = PagingConstants.PAGING_CONFIG, pagingSourceFactory = dataSource.asPagingSourceFactory()).liveData.cachedIn(this)

fun <T : Any> ViewModel.getPagingFlowAsFlow(dataSource : DataSource.Factory<Int, T>) =
    Pager(config = PagingConstants.PAGING_CONFIG, pagingSourceFactory = dataSource.asPagingSourceFactory()).flow.cachedIn(viewModelScope)

fun <T : Any> ViewModel.getPagingDataAsLiveData(pagingSource : PagingSource<Int, T>) =
    Pager(config = PagingConstants.PAGING_CONFIG){ pagingSource }.liveData.cachedIn(this)

fun <T : Any> ViewModel.getPagingFlowAsFlow(pagingSource : PagingSource<Int, T>) =
    Pager(config = PagingConstants.PAGING_CONFIG){ pagingSource }.flow.cachedIn(viewModelScope)


inline fun ViewModel.launch(
    dispatcher: CoroutineContext = EmptyCoroutineContext,
    scope: CoroutineScope = viewModelScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launch(dispatcher) {
        block.invoke(this)
    }
}