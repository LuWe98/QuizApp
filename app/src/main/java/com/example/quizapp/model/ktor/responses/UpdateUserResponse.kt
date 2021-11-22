package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserResponse(
    val responseType: UpdateUserResponseType,
) {
    enum class UpdateUserResponseType(@StringRes val messageRes: Int) {
        UPDATE_SUCCESSFUL(0),
        NOT_ACKNOWLEDGED(0),
        USERNAME_ALREADY_TAKEN(0),
        LAST_CHANGE_TO_CLOSE(0),
        USER_DOES_NOT_EXIST(0)
    }
}
