package com.example.quizapp.model.ktor.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.quizapp.extensions.log

open class SimplePagingSource<T : Any>(
    private val getRefreshKeyAction: (PagingState<Int, T>) -> (Int?) = getDefaultRefreshKeyAction(),
    private val getDataAction: suspend (Int) -> (List<T>)
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>) = getRefreshKeyAction.invoke(state)

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> = try {
        val page = params.key ?: PagingConfigValues.DEFAULT_INITIAL_PAGE_INDEX
        val response = getDataAction.invoke(page)
        LoadResult.Page(
            data = response,
            prevKey = if (page == PagingConfigValues.DEFAULT_INITIAL_PAGE_INDEX) null else page - 1,
            nextKey = if (response.isEmpty()) null else page + 1
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    companion object {
        fun <T: Any> getDefaultRefreshKeyAction() : (PagingState<Int, T>) -> (Int?) = { state ->
            state.anchorPosition?.let {
                state.closestPageToPosition(it).let { anchor ->
                    anchor?.prevKey?.plus(1) ?: anchor?.nextKey?.minus(1)
                }
            }
        }
    }
}