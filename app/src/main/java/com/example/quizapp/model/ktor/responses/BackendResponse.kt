package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import com.example.quizapp.R
import kotlinx.serialization.Serializable

sealed class BackendResponse {

    @Serializable
    data class BasicResponse(
        val isSuccessful: Boolean,
        val message : String
    ) : BackendResponse()


    @Serializable
    data class LoginUserResponse(
        val isSuccessful: Boolean,
        val userId : String?,
        val responseType: LoginUserResponseType,
    ) : BackendResponse() {
        enum class LoginUserResponseType(@StringRes val messageRes: Int) {
            LOGIN_SUCCESSFUL(R.string.userLoggedInSuccessfully),
            USER_NAME_OR_PASSWORD_WRONG(R.string.errorUserNameOrPassWordWrong)
        }
    }

    @Serializable
    data class RegisterUserResponse(
        val isSuccessful: Boolean,
        val responseType: RegisterUserResponseType,
    ) : BackendResponse() {
        enum class RegisterUserResponseType(@StringRes val messageRes: Int) {
            REGISTER_SUCCESSFUL(R.string.userRegisteredSuccessfully),
            USER_ALREADY_EXISTS(R.string.errorUserAlreadyExists)
        }
    }

    @Serializable
    data class UpdateUserResponse(
        val isSuccessful: Boolean,
        val responseType: UpdateUserResponseType,
    ) : BackendResponse() {
        enum class UpdateUserResponseType(@StringRes val messageRes: Int){
            UPDATE_SUCCESSFUL(0),
            USER_ALREADY_EXISTS(0),
            LAST_CHANGE_TO_CLOSE(0)
        }
    }
}