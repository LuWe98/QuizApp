package com.example.quizapp.model.ktor.paging

import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.databases.mongodb.documents.user.User

class UserAdminPagingSource (
    private val backendRepository: BackendRepository,
    private val searchQuery: String,
    private val roles: Set<Role>
) : BasicPagingSource<User>(
    getDataAction = { page -> backendRepository.getPagedUsersAdmin(PagingConfigValues.PAGE_SIZE, page, searchQuery, roles) }
)