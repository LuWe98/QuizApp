package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class ChangeQuestionnaireVisibilityResponse(
    val responseType: ChangeQuestionnaireVisibilityResponseType
) {
    enum class ChangeQuestionnaireVisibilityResponseType{
        SUCCESSFUL,
        ERROR
    }
}