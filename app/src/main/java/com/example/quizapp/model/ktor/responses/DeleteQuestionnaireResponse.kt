package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class DeleteQuestionnaireResponse(
    val responseType: DeleteQuestionnaireResponseType
)  {
    enum class  DeleteQuestionnaireResponseType {
        SUCCESSFUL,
        ERROR
    }
}
