package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.datastore.datawrappers.ManageUsersOrderBy
import kotlinx.serialization.Serializable

@Serializable
data class GetPagedUserAdminRequest(
    val limit: Int,
    val page: Int,
    val searchString: String,
    val roles: Set<Role>,
    val orderBy: ManageUsersOrderBy,
    val ascending: Boolean
)