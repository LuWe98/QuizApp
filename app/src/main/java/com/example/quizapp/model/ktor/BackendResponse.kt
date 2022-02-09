package com.example.quizapp.model.ktor

import android.content.Context
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.databases.dto.MongoBrowsableQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.MongoCourseOfStudies
import com.example.quizapp.model.databases.mongodb.documents.MongoFaculty
import com.example.quizapp.model.databases.mongodb.documents.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.MongoQuestionnaire
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.ktor.paging.BrowsableQuestionnairePageKeys
import kotlinx.serialization.Serializable

sealed class BackendResponse {

    @Serializable
    class BasicResponse(val isSuccessful: Boolean): BackendResponse()

    @Serializable
    data class GetPagedQuestionnairesWithPageKeysResponse(
        val previousKeys: BrowsableQuestionnairePageKeys?,
        val questionnaires: List<MongoBrowsableQuestionnaire>
    )


    @Serializable
    class TestResponse(val isSuccessful: Boolean, val errorType: TestResponseErrorType? = null): BackendResponse() {
        enum class TestResponseErrorType {
            ERROR_TYPE_ONE,
            ERROR_TYPE_TWO
        }
    }


    @Serializable
    data class ChangePasswordResponse(
        val newToken: String? = null,
        val responseType: ChangePasswordResponseType
    ): BackendResponse() {
        enum class ChangePasswordResponseType(@StringRes val messageRes: Int){
            SUCCESSFUL(R.string.passwordChangedSuccessfully),
            NOT_ACKNOWLEDGED(R.string.errorCouldNotChangePassword)
        }
    }


    @Serializable
    data class ChangeQuestionnaireVisibilityResponse(
        val responseType: ChangeQuestionnaireVisibilityResponseType
    ): BackendResponse() {
        enum class ChangeQuestionnaireVisibilityResponseType{
            SUCCESSFUL,
            NOT_ACKNOWLEDGED
        }
    }


    @Serializable
    data class CreateUserResponse(
        val responseType: CreateUserResponseType,
    ): BackendResponse() {
        enum class CreateUserResponseType(@StringRes val messageRes: Int) {
            CREATION_SUCCESSFUL(R.string.userWasCreated),
            NOT_ACKNOWLEDGED(R.string.errorCouldNotCreateUser),
            USER_ALREADY_EXISTS(R.string.errorUserAlreadyExists)
        }
    }


    @Serializable
    data class DeleteCourseOfStudiesResponse(
        val responseType: DeleteCourseOfStudiesResponseType
    ): BackendResponse() {
        enum class  DeleteCourseOfStudiesResponseType {
            SUCCESSFUL,
            NOT_ACKNOWLEDGED
        }
    }


    @Serializable
    data class DeleteFacultyResponse(
        val responseType: DeleteFacultyResponseType
    ): BackendResponse() {
        enum class  DeleteFacultyResponseType {
            SUCCESSFUL,
            NOT_ACKNOWLEDGED
        }
    }


    @Serializable
    data class DeleteFilledQuestionnaireResponse(
        val responseType: DeleteFilledQuestionnaireResponseType
    ): BackendResponse() {
        enum class  DeleteFilledQuestionnaireResponseType {
            SUCCESSFUL,
            NOT_ACKNOWLEDGED
        }
    }


    @Serializable
    data class DeleteQuestionnaireResponse(
        val responseType: DeleteQuestionnaireResponseType
    ): BackendResponse() {
        enum class  DeleteQuestionnaireResponseType {
            SUCCESSFUL,
            NOT_ACKNOWLEDGED
        }
    }


    @Serializable
    data class DeleteUserResponse(
        val responseType: DeleteUserResponseType,
    ): BackendResponse() {
        enum class DeleteUserResponseType {
            SUCCESSFUL,
            NOT_ACKNOWLEDGED
        }
    }


    @Serializable
    data class GetQuestionnaireResponse(
        val responseType: GetQuestionnaireResponseType,
        val mongoQuestionnaire: MongoQuestionnaire? = null
    ): BackendResponse() {
        enum class GetQuestionnaireResponseType {
            SUCCESSFUL,
            QUESTIONNAIRE_NOT_FOUND,
        }
    }


    @Serializable
    data class InsertCourseOfStudiesResponse(
        val responseType: InsertCourseOfStudiesResponseType
    ): BackendResponse() {
        enum class  InsertCourseOfStudiesResponseType(@StringRes val messageRes: Int) {
            SUCCESSFUL(R.string.successfullySavedCourseOfStudies),
            ABBREVIATION_ALREADY_USED(R.string.errorCourseOfStudiesAbbreviationAlreadyUsed),
            NOT_ACKNOWLEDGED(R.string.errorCouldNotSaveCourseOfStudies)
        }
    }


    @Serializable
    data class InsertFacultyResponse(
        val responseType: InsertFacultyResponseType
    ): BackendResponse() {
        enum class  InsertFacultyResponseType(@StringRes val messageRes: Int) {
            SUCCESSFUL(R.string.successfullySavedFaculty),
            ABBREVIATION_ALREADY_USED(R.string.errorFacultyAbbreviationAlreadyUsed),
            NAME_ALREADY_USED(R.string.errorFacultyNameAlreadyUsed),
            NOT_ACKNOWLEDGED(R.string.errorCouldNotSaveFaculty)
        }
    }


    @Serializable
    data class InsertFilledQuestionnaireResponse(
        val responseType: InsertFilledQuestionnaireResponseType
    ): BackendResponse() {
        enum class  InsertFilledQuestionnaireResponseType {
            SUCCESSFUL,
            QUESTIONNAIRE_DOES_NOT_EXIST_ANYMORE,
            NOT_ACKNOWLEDGED
        }
    }


    @Serializable
    data class InsertFilledQuestionnairesResponse(
        val notInsertedQuestionnaireIds: List<String> = emptyList(),
        val responseType: InsertFilledQuestionnairesResponseType
    ): BackendResponse() {
        enum class  InsertFilledQuestionnairesResponseType {
            SUCCESSFUL,
            NOT_ACKNOWLEDGED
        }
    }


    @Serializable
    data class InsertQuestionnairesResponse(
        val responseType: InsertQuestionnairesResponseType
    ): BackendResponse() {
        enum class  InsertQuestionnairesResponseType {
            SUCCESSFUL,
            NOT_ACKNOWLEDGED
        }
    }


    @Serializable
    data class LoginUserResponse(
        val userId: String? = null,
        val role: Role? = null,
        val lastModifiedTimeStamp: Long? = null,
        val token: String? = null,
        val responseType: LoginUserResponseType,
    ): BackendResponse() {
        enum class LoginUserResponseType(@StringRes val messageRes: Int) {
            LOGIN_SUCCESSFUL(R.string.userLoggedInSuccessfully),
            USER_NAME_OR_PASSWORD_WRONG(R.string.errorUserNameOrPassWordWrong)
        }
    }


    @Serializable
    data class RefreshJwtTokenResponse(
        val token: String?
    ): BackendResponse()

    @Serializable
    data class RegisterUserResponse(
        val responseType: RegisterUserResponseType,
    ): BackendResponse() {
        enum class RegisterUserResponseType(@StringRes val messageRes: Int)  {
            REGISTER_SUCCESSFUL(R.string.userRegisteredSuccessfully),
            NOT_ACKNOWLEDGED(R.string.userRegistrationFailed),
            USER_ALREADY_EXISTS(R.string.errorUserAlreadyExists)
        }
    }


    @Serializable
    data class ShareQuestionnaireWithUserResponse(
        val responseType: ShareQuestionnaireWithUserResponseType
    ): BackendResponse() {
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


    @Serializable
    data class SyncCoursesOfStudiesResponse(
        val coursesOfStudiesToInsert: List<MongoCourseOfStudies>,
        val coursesOfStudiesToUpdate: List<MongoCourseOfStudies>,
        val courseOfStudiesIdsToDelete: List<String>
    ): BackendResponse() {
        fun isEmpty() = coursesOfStudiesToInsert.isEmpty() && coursesOfStudiesToUpdate.isEmpty() && courseOfStudiesIdsToDelete.isEmpty()
    }


    @Serializable
    data class SyncFacultiesResponse(
        val facultiesToInsert: List<MongoFaculty>,
        val facultiesToUpdate: List<MongoFaculty>,
        val facultyIdsToDelete: List<String>
    ): BackendResponse() {
        fun isEmpty() = facultiesToInsert.isEmpty() && facultiesToUpdate.isEmpty() && facultyIdsToDelete.isEmpty()
    }


    @Serializable
    data class SyncQuestionnairesResponse(
        val mongoQuestionnaires: List<MongoQuestionnaire>,
        val mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>,
        val questionnaireIdsToUnsync: List<String>
    ): BackendResponse() {
        fun isEmpty() = mongoQuestionnaires.isEmpty() && mongoFilledQuestionnaires.isEmpty() && questionnaireIdsToUnsync.isEmpty()
    }


    @Serializable
    data class SyncUserDataResponse(
        val role: Role? = null,
        val lastModifiedTimestamp: Long? = null,
        val responseType: SyncUserDataResponseType
    ): BackendResponse() {
        enum class SyncUserDataResponseType {
            DATA_UP_TO_DATE,
            DATA_CHANGED
        }
    }


    @Serializable
    data class UpdateUserResponse(
        val responseType: UpdateUserResponseType,
    ): BackendResponse() {
        enum class UpdateUserResponseType {
            UPDATE_SUCCESSFUL,
            NOT_ACKNOWLEDGED,
            USERNAME_ALREADY_TAKEN,
            LAST_CHANGE_TO_CLOSE,
            USER_DOES_NOT_EXIST
        }
    }
}