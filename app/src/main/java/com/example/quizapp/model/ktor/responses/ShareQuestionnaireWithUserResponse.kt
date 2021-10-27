package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class ShareQuestionnaireWithUserResponse(
    val responseType: ShareQuestionnaireWithUserResponseType
) {
    enum class ShareQuestionnaireWithUserResponseType {
        SUCCESSFUL,
        ALREADY_SHARED_WITH_USER,
        USER_DOES_NOT_EXIST,
        QUESTIONNAIRE_DOES_NOT_EXIST,
        ERROR
    }
}