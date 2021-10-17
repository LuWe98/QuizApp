package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import com.example.quizapp.R
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserResponse(
    val isSuccessful: Boolean,
    val responseType: RegisterUserResponseType,
) {
    enum class RegisterUserResponseType(@StringRes val messageRes: Int)  {
        REGISTER_SUCCESSFUL(R.string.userRegisteredSuccessfully),
        USER_ALREADY_EXISTS(R.string.errorUserAlreadyExists)
    }
}