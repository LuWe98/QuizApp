package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import com.example.quizapp.R
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserResponse(
    val responseType: CreateUserResponseType,
) {
    enum class CreateUserResponseType(@StringRes val messageRes: Int) {
        CREATION_SUCCESSFUL(R.string.userWasCreated),
        NOT_ACKNOWLEDGED(R.string.errorCouldNotCreateUser),
        USER_ALREADY_EXISTS(R.string.errorUserAlreadyExists)
    }
}