package com.example.quizapp.model.ktor.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

open class BasicPagingSource <T: Any> (
    private val getRefreshKeyAction : (Int) -> (Int?) = { anchorPosition -> anchorPosition/ PagingConfigValues.PAGE_SIZE },
    private val getDataAction : suspend (Int) -> (List<T>)) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let(getRefreshKeyAction)

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try{
            val page = params.key ?: PagingConfigValues.INITIAL_PAGE_INDEX
            val response = getDataAction.invoke(page)
            LoadResult.Page(
                data = response,
                prevKey = if(page == PagingConfigValues.INITIAL_PAGE_INDEX) null else page -1,
                nextKey = if(response.isEmpty()) null else page + 1
            )

        } catch (e: Exception) {
            //HANDLE ERRORS HERE
            LoadResult.Error(e)
        }
    }
}