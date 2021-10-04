package com.example.quizapp.model.ktor.requests

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
        val newUserName: String
    ) : BackendRequest()

    @Serializable
    data class DeleteAnswerRequest(
        val questionnaireId: String,
        val answerId: String
    ) : BackendRequest()

    @Serializable
    data class DeleteQuestionnaireRequest(
        val questionnaireId: String
    ) : BackendRequest()

    @Serializable
    data class DeleteQuestionRequest(
        val questionnaireId: String,
        val questionId: String
    ) : BackendRequest()

    @Serializable
    data class QuestionnairesRequest(
        val searchString: String
    ) : BackendRequest()

}