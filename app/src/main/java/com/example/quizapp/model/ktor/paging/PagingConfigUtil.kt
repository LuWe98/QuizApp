package com.example.quizapp.model.ktor.paging

import androidx.paging.PagingConfig

object PagingConfigUtil {
    const val DEFAULT_INITIAL_PAGE_INDEX = 1
    const val DEFAULT_PAGE_SIZE = 20
    private const val DEFAULT_MAX_ITEMS = 120

    val defaultPagingConfig
        get() = PagingConfig(
            pageSize = DEFAULT_PAGE_SIZE,
            maxSize = DEFAULT_MAX_ITEMS
        )
}