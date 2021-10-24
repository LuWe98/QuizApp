package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetQuestionnaireRequest(
    val questionnaireId: String
)
