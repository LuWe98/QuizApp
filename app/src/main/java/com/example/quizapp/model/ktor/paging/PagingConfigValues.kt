package com.example.quizapp.model.ktor.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig

object PagingConfigValues {
    const val DEFAULT_INITIAL_PAGE_INDEX = 1
    const val DEFAULT_PAGE_SIZE = 20
    const val DEFAULT_MAX_ITEMS = 120

    val defaultPagingConfig
        get() = PagingConfig(
            pageSize = DEFAULT_PAGE_SIZE,
            maxSize = DEFAULT_MAX_ITEMS
        )

    suspend inline fun <reified T: Any> getDefaultPager(crossinline getDataAction: suspend (Int) -> List<T>) = Pager(
        config = defaultPagingConfig,
        pagingSourceFactory = {
            DefaultPagingSource { page ->
                getDataAction.invoke(page)
            }
        }
    )
}