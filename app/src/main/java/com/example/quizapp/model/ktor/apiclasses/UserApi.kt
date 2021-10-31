package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.requests.*
import com.example.quizapp.model.ktor.responses.*
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

    suspend fun syncUserData(userId: String) : SyncUserDataResponse =
        client.post("user/sync"){
            body = SyncUserDataRequest(userId)
        }

    suspend fun refreshJwtToken(userName: String, password: String) : RefreshJwtTokenResponse =
        client.post("user/token") {
            body = RefreshJwtTokenRequest(userName, password)
        }

}