package com.example.quizapp.model.ktor.paging

import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.databases.mongodb.documents.user.User

class UserPagingSource (
    private val backendRepository: BackendRepository,
    private val searchQuery: String
) : BasicPagingSource<User>(
    getDataAction = { page -> backendRepository.getPagedUsers(PagingConfigValues.PAGE_SIZE, page, searchQuery) }
)