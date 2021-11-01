package com.example.quizapp.model.databases.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuestionnaireIdWithTimestamp(
    val id: String,
    val lastModifiedTimestamp: Long
)