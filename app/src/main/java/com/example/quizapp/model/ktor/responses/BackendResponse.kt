package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import com.example.quizapp.R

sealed class BackendResponse {


    data class BasicResponse(
        val isSuccessful: Boolean,
        val message : String
    ) : BackendResponse()


    data class LoginResponse<T>(
        val isSuccessful: Boolean,
        val loginResponseType: LoginResponseType,
        val data: T? = null
    ) : BackendResponse() {
        enum class LoginResponseType constructor(@StringRes val messageRes: Int) {
            LOGIN_SUCCESSFUL(R.string.userLoggedInSuccessfully),
            USER_NAME_OR_PASSWORD_WRONG(R.string.errorUserNameOrPassWordWrong)
        }
    }


    data class RegisterResponse<T>(
        val isSuccessful: Boolean,
        val registerResponseType: RegisterResponseType,
        val data: T? = null
    ) : BackendResponse() {
        enum class RegisterResponseType constructor(@StringRes val messageRes: Int) {
            REGISTER_SUCCESSFUL(R.string.userRegisteredSuccessfully),
            USER_ALREADY_EXISTS(R.string.errorUserAlreadyExists)
        }
    }
}