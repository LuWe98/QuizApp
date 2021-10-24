package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class InsertQuestionnaireResponse(
    val responseType: InsertQuestionnaireResponseType
) {
    enum class  InsertQuestionnaireResponseType {
        SUCCESSFUL,
        NOT_ACKNOWLEDGED,
        ERROR
    }
}
