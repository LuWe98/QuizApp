package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class InsertFilledQuestionnaireResponse(
    val responseType: InsertFilledQuestionnaireResponseType
) {
    enum class  InsertFilledQuestionnaireResponseType {
        SUCCESSFUL,
        EMPTY_INSERTION_SKIPPED,
        QUESTIONNAIRE_DOES_NOT_EXIST_ANYMORE,
        ERROR
    }
}
