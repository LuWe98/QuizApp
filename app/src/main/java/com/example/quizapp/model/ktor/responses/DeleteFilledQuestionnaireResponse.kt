package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class DeleteFilledQuestionnaireResponse(
    val responseType: DeleteFilledQuestionnaireResponseType
) {
    enum class  DeleteFilledQuestionnaireResponseType {
        SUCCESSFUL,
        ERROR
    }
}
