package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserNameRequest(
    val newUserName: String
)