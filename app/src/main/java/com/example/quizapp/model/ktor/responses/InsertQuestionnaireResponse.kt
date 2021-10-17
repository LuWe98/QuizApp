package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class InsertQuestionnaireResponse(
    val isSuccessful: Boolean,
    val responseType: InsertQuestionnaireResponseType
) {
    enum class  InsertQuestionnaireResponseType {
        INSERTED,
        REPLACED,
        NOT_ACKNOWLEDGED,
        ERROR
    }
}
