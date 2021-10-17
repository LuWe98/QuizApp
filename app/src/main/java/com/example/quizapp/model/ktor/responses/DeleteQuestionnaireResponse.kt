package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class DeleteQuestionnaireResponse(
    val isSuccessful: Boolean,
    val responseType: DeleteQuestionnaireResponseType
)  {
    enum class  DeleteQuestionnaireResponseType {
        SUCCESSFUL,
        ERROR
    }
}
