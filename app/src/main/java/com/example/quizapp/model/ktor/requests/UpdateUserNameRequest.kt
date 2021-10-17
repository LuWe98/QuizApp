package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserNameRequest(
    val userId: String,
    val newUserName: String
)
