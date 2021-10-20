package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteFilledQuestionnaireRequest(
    val questionnaireIds: List<String>
)
