package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserResponse(
    val isSuccessful: Boolean,
    val responseType: UpdateUserResponseType,
) {
    enum class UpdateUserResponseType(@StringRes val messageRes: Int) {
        UPDATE_SUCCESSFUL(0),
        USER_ALREADY_EXISTS(0),
        LAST_CHANGE_TO_CLOSE(0),
        USER_DOES_NOT_EXIST(0)
    }
}
