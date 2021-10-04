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

    suspend fun loginUser(email: String, password: String): LoginUserResponse =
        client.post("/user/login") {
            body = LoginUserRequest(email, password)
        }

    suspend fun registerUser(email: String, password: String, courseOfStudies : String): RegisterUserResponse =
        client.post("/user/register") {
            body = RegisterUserRequest(email, password, courseOfStudies)
        }

    suspend fun updateUser(newUserName : String): UpdateUserResponse =
        client.post("/user/update") {
            body = UpdateUserRequest(newUserName)
        }

    suspend fun deleteUser(): Boolean = client.post("/user/delete")
}