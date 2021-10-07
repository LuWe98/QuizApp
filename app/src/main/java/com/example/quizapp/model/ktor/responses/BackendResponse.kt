package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import kotlinx.serialization.Serializable

sealed class BackendResponse {

    @Serializable
    data class BasicResponse(
        val isSuccessful: Boolean,
        val responseType : BasicResponseType
    ) : BackendResponse() {
        enum class BasicResponseType {
            SUCCESSFUL,
            ERROR
        }
    }

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

    @Serializable
    data class DeleteUserResponse(
        val isSuccessful: Boolean,
        val responseType: DeleteUserResponseType,
    ) : BackendResponse() {
        enum class DeleteUserResponseType {
            DELETION_SUCCESSFUL,
            USER_COULD_NOT_BE_DELETED
        }
    }

    @Serializable
    data class InsertQuestionnaireResponse(
        val isSuccessful: Boolean,
        val responseType: InsertQuestionnaireResponseType
    ) : BackendResponse() {
        enum class  InsertQuestionnaireResponseType {
            INSERT_SUCCESSFUL,
            ERROR
        }
    }

    @Serializable
    data class InsertFilledQuestionnaireResponse(
        val isSuccessful: Boolean,
        val responseType: InsertFilledQuestionnaireResponseType
    ) : BackendResponse() {
        enum class  InsertFilledQuestionnaireResponseType {
            INSERT_SUCCESSFUL,
            ERROR,
            EMPTY_FILLED_QUESTIONNAIRE_NOT_INSERTED,
            QUESTIONNAIRE_DOES_NOT_EXIST_ANYMORE
        }
    }

    @Serializable
    data class DeleteQuestionnaireResponse(
        val isSuccessful: Boolean,
        val responseType: DeleteQuestionnaireResponseType
    ) : BackendResponse() {
        enum class  DeleteQuestionnaireResponseType {
            SUCCESSFUL,
            ERROR
        }
    }

    @Serializable
    data class DeleteFilledQuestionnaireResponse(
        val isSuccessful: Boolean,
        val responseType: DeleteFilledQuestionnaireResponseType
    ) : BackendResponse() {
        enum class  DeleteFilledQuestionnaireResponseType {
            SUCCESSFUL,
            ERROR
        }
    }


    @Serializable
    data class GetAllSyncedQuestionnairesResponse(
        val mongoQuestionnaires: List<MongoQuestionnaire>,
        val mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>,
        val responseType: GetAllSyncedQuestionnairesResponseType
    ) : BackendResponse(){
        enum class  GetAllSyncedQuestionnairesResponseType {
            SUCCESSFUL,
            ERROR
        }
    }
}