package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserResponse(
    val isSuccessful: Boolean,
    val responseType: DeleteUserResponseType,
)  {
    enum class DeleteUserResponseType {
        DELETION_SUCCESSFUL,
        USER_COULD_NOT_BE_DELETED
    }
}