package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.requests.LoginUserRequest
import com.example.quizapp.model.ktor.requests.RegisterUserRequest
import com.example.quizapp.model.ktor.responses.BackendResponse.*
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserApi @Inject constructor(
    private val ktorClient: HttpClient
) {

    suspend fun loginUser(email: String, password: String): LoginResponse<Nothing> =
        ktorClient.post("/login") {
            body = LoginUserRequest(email, password)
        }


    suspend fun registerUser(email: String, username: String, password: String): RegisterResponse<Nothing> =
        ktorClient.post("/register") {
            body = RegisterUserRequest(email, username, password)
        }
}