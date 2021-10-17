package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.requests.*
import com.example.quizapp.model.ktor.responses.DeleteUserResponse
import com.example.quizapp.model.ktor.responses.LoginUserResponse
import com.example.quizapp.model.ktor.responses.RegisterUserResponse
import com.example.quizapp.model.ktor.responses.UpdateUserResponse
import com.example.quizapp.model.mongodb.documents.user.Role
import com.example.quizapp.model.mongodb.documents.user.User
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

    suspend fun updateUsername(userId: String, newUserName: String): UpdateUserResponse =
        client.post("/user/update/username") {
            body = UpdateUserNameRequest(userId, newUserName)
        }

    suspend fun updateUserRole(userId: String, newRole: Role): UpdateUserResponse =
        client.post("/user/update/role") {
            body = UpdateUserRoleRequest(userId, newRole)
        }

    suspend fun deleteUser(userId: String): DeleteUserResponse =
        client.post("/user/delete") {
            body = DeleteUserRequest(userId)
        }

    suspend fun getPagedUsers(limit: Int, page: Int, searchString: String) : List<User> =
        client.post("/users/paged") {
            body = GetPagedUserRequest(limit, page, searchString)
        }
}