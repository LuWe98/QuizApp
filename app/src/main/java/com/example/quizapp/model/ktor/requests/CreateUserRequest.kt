package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.mongodb.documents.user.Role
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val userName: String,
    val password: String,
    val role: Role
)
