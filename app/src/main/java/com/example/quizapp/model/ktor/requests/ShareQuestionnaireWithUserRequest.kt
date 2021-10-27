package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class ShareQuestionnaireWithUserRequest(
    val questionnaireId: String,
    val userName: String,
    val canEdit: Boolean
)