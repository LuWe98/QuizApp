package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.mongodb.documents.user.Role
import kotlinx.serialization.Serializable

@Serializable
data class LoginUserResponse(
    val isSuccessful: Boolean,
    val userId: String? = null,
    val role: Role? = null,
    val responseType: LoginUserResponseType,
) {
    enum class LoginUserResponseType(@StringRes val messageRes: Int) {
        LOGIN_SUCCESSFUL(R.string.userLoggedInSuccessfully),
        USER_NAME_OR_PASSWORD_WRONG(R.string.errorUserNameOrPassWordWrong)
    }
}
