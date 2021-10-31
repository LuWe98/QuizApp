package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class RefreshJwtTokenResponse(
    val token: String?
)
