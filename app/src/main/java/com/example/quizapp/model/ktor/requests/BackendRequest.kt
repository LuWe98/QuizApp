package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.room.entities.LocallyDeletedQuestionnaire
import kotlinx.serialization.Serializable


sealed class BackendRequest {

    @Serializable
    data class LoginUserRequest(
        val userName: String,
        val password: String
    ) : BackendRequest()


    @Serializable
    data class RegisterUserRequest(
        val userName: String,
        val password: String,
        val courseOfStudies: String
    ) : BackendRequest()


    @Serializable
    data class UpdateUserRequest(
        val userId: String,
        val newUserName: String
    ) : BackendRequest()


    @Serializable
    data class DeleteUserRequest(
        val userId: String
    ) : BackendRequest()


    @Serializable
    data class InsertQuestionnaireRequest(
        val mongoQuestionnaire: MongoQuestionnaire
    ) : BackendRequest()


    @Serializable
    data class InsertFilledQuestionnaireRequest(
        val shouldBeIgnoredWhenAnotherIsPresent : Boolean,
        val mongoFilledQuestionnaire: MongoFilledQuestionnaire
    ) : BackendRequest()


    @Serializable
    data class SyncQuestionnairesRequest(
        val syncedQuestionnaireIdsWithTimestamp : List<QuestionnaireIdWithTimestamp>,
        val unsyncedQuestionnaireIds: List<String>,
        val locallyDeletedQuestionnaireIds: List<String>
    ) : BackendRequest()


    @Serializable
    data class GetPagedQuestionnairesRequest(
        val limit: Int,
        val page: Int,
        val searchString: String
    ) : BackendRequest()


    @Serializable
    data class GetPagedUsersRequest(
        val limit: Int ,
        val page: Int,
        val searchString: String
    ) : BackendRequest()


    @Serializable
    data class DeleteQuestionnaireRequest(
        val questionnaireIds: List<String>
    ) : BackendRequest()


    @Serializable
    data class DeleteFilledQuestionnaireRequest(
        val userId: String,
        val questionnaireIds: List<String>
    ) : BackendRequest()
}