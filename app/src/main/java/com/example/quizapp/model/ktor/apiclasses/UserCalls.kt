package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.requests.LoginUserRequest
import com.example.quizapp.model.ktor.requests.RegisterUserRequest
import com.example.quizapp.model.ktor.responses.BasicResponse
import com.example.quizapp.utils.Constants
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserCalls @Inject constructor(
    private val ktorClient: HttpClient
) {

    suspend fun loginUser(email: String, password: String) : BasicResponse<*> =
        ktorClient.post("${Constants.EXTERNAL_DATABASE_URL}/login") {
            contentType(ContentType.Application.Json)
            body = LoginUserRequest(email, password)
        }

    suspend fun registerUser(email: String, username: String, password: String) : BasicResponse<*> =
        ktorClient.post("${Constants.EXTERNAL_DATABASE_URL}/register") {
            contentType(ContentType.Application.Json)
            body = RegisterUserRequest(email, username, password)
        }
}