package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import com.example.quizapp.R
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordResponse(
    val newToken: String? = null,
    val responseType: ChangePasswordResponseType
) {
    enum class ChangePasswordResponseType(@StringRes val messageRes: Int){
        SUCCESSFUL(R.string.passwordChangedSuccessfully),
        NOT_ACKNOWLEDGED(R.string.errorCouldNotChangePassword)
    }
}