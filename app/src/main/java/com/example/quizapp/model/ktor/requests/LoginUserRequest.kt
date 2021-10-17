package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginUserRequest(
    val userName: String,
    val password: String
)