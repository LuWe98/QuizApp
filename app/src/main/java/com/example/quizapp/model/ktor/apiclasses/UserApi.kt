package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.requests.LoginUserRequest
import com.example.quizapp.model.ktor.requests.RegisterUserRequest
import com.example.quizapp.model.ktor.requests.UpdateUserNameRequest
import com.example.quizapp.model.ktor.responses.DeleteUserResponse
import com.example.quizapp.model.ktor.responses.LoginUserResponse
import com.example.quizapp.model.ktor.responses.RegisterUserResponse
import com.example.quizapp.model.ktor.responses.UpdateUserResponse
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

    suspend fun registerUser(newUserName: String, password: String): RegisterUserResponse =
        client.post("/user/register") {
            body = RegisterUserRequest(newUserName, password)
        }

    suspend fun updateUsername(newUserName: String): UpdateUserResponse =
        client.post("/user/update/username") {
            body = UpdateUserNameRequest(newUserName)
        }

    suspend fun deleteSelf(): DeleteUserResponse = client.delete("/user/delete")

}