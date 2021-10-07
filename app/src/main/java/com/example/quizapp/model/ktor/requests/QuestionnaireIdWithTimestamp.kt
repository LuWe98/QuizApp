package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class QuestionnaireIdWithTimestamp(
    val id: String,
    val lastModifiedTimestamp: Long
)