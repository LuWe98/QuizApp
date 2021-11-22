package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserResponse(
    val responseType: DeleteUserResponseType,
)  {
    enum class DeleteUserResponseType {
        SUCCESSFUL,
        NOT_ACKNOWLEDGED
    }
}