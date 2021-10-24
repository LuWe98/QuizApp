package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserResponse(
    val responseType: DeleteUserResponseType,
)  {
    enum class DeleteUserResponseType {
        SUCCESSFUL,
        USER_COULD_NOT_BE_DELETED
    }
}