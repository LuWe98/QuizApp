package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val newPassword: String
)