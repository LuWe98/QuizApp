package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUsersRequest(
    val userIds: List<String>
)
