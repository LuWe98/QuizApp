package com.example.quizapp.model.ktor

import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.databases.mongodb.documents.MongoCourseOfStudies
import com.example.quizapp.model.databases.mongodb.documents.MongoFaculty
import com.example.quizapp.model.databases.mongodb.documents.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.MongoQuestionnaire
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.datastore.datawrappers.ManageUsersOrderBy
import com.example.quizapp.model.datastore.datawrappers.RemoteQuestionnaireOrderBy
import kotlinx.serialization.Serializable

sealed class BackendRequest {

    @Serializable
    data class ChangePasswordRequest(
        val newPassword: String
    ): BackendRequest()


    @Serializable
    data class ChangeQuestionnaireVisibilityRequest(
        val questionnaireId: String,
        val newVisibility: QuestionnaireVisibility
    ): BackendRequest()


    @Serializable
    data class CreateUserRequest(
        val userName: String,
        val password: String,
        val role: Role
    ): BackendRequest()


    @Serializable
    data class DeleteCourseOfStudiesRequest(
        val courseOfStudiesId: String
    ): BackendRequest()


    @Serializable
    data class DeleteFacultyRequest(
        val facultyId: String
    ): BackendRequest()


    @Serializable
    data class DeleteFilledQuestionnaireRequest(
        val questionnaireIds: List<String>
    ): BackendRequest()


    @Serializable
    data class DeleteQuestionnaireRequest(
        val questionnaireIds: List<String>
    ): BackendRequest()


    @Serializable
    data class DeleteUserRequest(
        val userId: String
    ): BackendRequest()


    @Serializable
    data class GetPagedAuthorsRequest(
        val limit: Int,
        val page: Int,
        val searchString: String
    ): BackendRequest()


    @Serializable
    data class GetPagedUserAdminRequest(
        val limit: Int,
        val page: Int,
        val searchString: String,
        val roles: Set<Role>,
        val orderBy: ManageUsersOrderBy,
        val ascending: Boolean
    ): BackendRequest()


    @Serializable
    data class GetPagedQuestionnairesRequest(
        val limit: Int,
        val page: Int,
        val searchString: String,
        val questionnaireIdsToIgnore: List<String>,
        val facultyIds: List<String>,
        val courseOfStudiesIds: List<String>,
        val authorIds: List<String>,
        val remoteQuestionnaireOrderBy: RemoteQuestionnaireOrderBy,
        val ascending: Boolean
    ): BackendRequest()


    @Serializable
    data class GetQuestionnaireRequest(
        val questionnaireId: String
    ): BackendRequest()


    @Serializable
    data class InsertCourseOfStudiesRequest(
        val courseOfStudies: MongoCourseOfStudies
    ): BackendRequest()


    @Serializable
    data class InsertFacultyRequest(
        val faculty: MongoFaculty
    ): BackendRequest()


    @Serializable
    data class InsertFilledQuestionnaireRequest(
        val shouldBeIgnoredWhenAnotherIsPresent : Boolean,
        val mongoFilledQuestionnaire: MongoFilledQuestionnaire
    ): BackendRequest()


    @Serializable
    data class InsertFilledQuestionnairesRequest(
        val mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>
    ): BackendRequest()


    @Serializable
    data class InsertQuestionnairesRequest(
        val mongoQuestionnaires: List<MongoQuestionnaire>
    ): BackendRequest()


    @Serializable
    data class LoginUserRequest(
        val userName: String,
        val password: String
    ): BackendRequest()


    @Serializable
    data class RefreshJwtTokenRequest(
        val userName: String,
        val password: String
    ): BackendRequest()


    @Serializable
    data class RegisterUserRequest(
        val userName: String,
        val password: String
    ): BackendRequest()


    @Serializable
    data class ShareQuestionnaireWithUserRequest(
        val questionnaireId: String,
        val userName: String,
        val canEdit: Boolean
    ): BackendRequest()


    @Serializable
    data class SyncCoursesOfStudiesRequest(
        val localCourseOfStudiesWithTimeStamp: List<CourseOfStudiesIdWithTimeStamp>
    ): BackendRequest()


    @Serializable
    data class SyncFacultiesRequest(
        val localFacultyIdsWithTimeStamp: List<FacultyIdWithTimeStamp>,
    ): BackendRequest()


    @Serializable
    data class SyncQuestionnairesRequest(
        val syncedQuestionnaireIdsWithTimestamp : List<QuestionnaireIdWithTimestamp>,
        val unsyncedQuestionnaireIds: List<String>,
        val locallyDeletedQuestionnaireIds: List<String>
    ): BackendRequest()


    @Serializable
    data class SyncUserDataRequest(
        val userId: String
    ): BackendRequest()


    @Serializable
    data class UpdateUserNameRequest(
        val newUserName: String
    ): BackendRequest()


    @Serializable
    data class UpdateUserRoleRequest(
        val userId: String,
        val newRole: Role
    ): BackendRequest()

}