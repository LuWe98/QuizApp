package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.mongodb.documents.user.Role
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRoleRequest(
    val userId: String,
    val newRole: Role
)
