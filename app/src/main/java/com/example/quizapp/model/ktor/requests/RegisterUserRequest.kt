package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    val userName: String,
    val password: String
)
