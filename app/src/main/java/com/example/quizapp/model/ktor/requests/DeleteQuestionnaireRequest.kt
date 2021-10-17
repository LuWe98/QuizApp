package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteQuestionnaireRequest(
    val questionnaireIds: List<String>
)
