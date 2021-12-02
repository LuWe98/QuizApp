package com.example.quizapp.model.ktor.responses

import android.content.Context
import com.example.quizapp.R
import kotlinx.serialization.Serializable

@Serializable
data class ShareQuestionnaireWithUserResponse(
    val responseType: ShareQuestionnaireWithUserResponseType
) {
    enum class ShareQuestionnaireWithUserResponseType {
        SUCCESSFUL,
        ALREADY_SHARED_WITH_USER,
        USER_DOES_NOT_EXIST,
        QUESTIONNAIRE_DOES_NOT_EXIST,
        NOT_ACKNOWLEDGED,
        USER_IS_OWNER_OF_QUESTIONNAIRE;


        fun getMessage(userName: String, context: Context) = when (this) {
            SUCCESSFUL -> context.getString(R.string.sharedWithUser, userName)
            ALREADY_SHARED_WITH_USER -> context.getString(R.string.errorUserCanAlreadySeeQuestionnaire)
            USER_DOES_NOT_EXIST -> context.getString(R.string.errorUserDoesNotExist)
            QUESTIONNAIRE_DOES_NOT_EXIST -> context.getString(R.string.errorQuestionnaireDoesNotExist)
            NOT_ACKNOWLEDGED -> context.getString(R.string.errorCouldNotShare)
            USER_IS_OWNER_OF_QUESTIONNAIRE -> context.getString(R.string.errorUserIsOwnerOfThisQuestionnaire)
        }
    }
}