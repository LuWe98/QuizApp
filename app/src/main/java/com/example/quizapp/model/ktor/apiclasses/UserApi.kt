package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.requests.BackendRequest.*
import com.example.quizapp.model.ktor.responses.BackendResponse.*
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun loginUser(newUserName: String, password: String): LoginUserResponse =
        client.post("/user/login") {
            body = LoginUserRequest(newUserName, password)
        }

    suspend fun registerUser(newUserName: String, password: String, courseOfStudies: String): RegisterUserResponse =
        client.post("/user/register") {
            body = RegisterUserRequest(newUserName, password, courseOfStudies)
        }

    suspend fun updateUser(userId: String, newUserName: String): UpdateUserResponse =
        client.post("/user/update") {
            body = UpdateUserRequest(userId, newUserName)
        }

    suspend fun deleteUser(userId: String): DeleteUserResponse =
        client.post("/user/delete") {
            body = DeleteUserRequest(userId)
        }

}