package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.requests.*
import com.example.quizapp.model.ktor.responses.DeleteUserResponse
import com.example.quizapp.model.ktor.responses.UpdateUserResponse
import com.example.quizapp.model.mongodb.documents.user.Role
import com.example.quizapp.model.mongodb.documents.user.User
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun updateUserRole(userId: String, newRole: Role): UpdateUserResponse =
        client.post("/admin/user/update/role") {
            body = UpdateUserRoleRequest(userId, newRole)
        }

    suspend fun getPagedUsers(limit: Int, page: Int, searchString: String) : List<User> =
        client.post("/admin/users/paged") {
            body = GetPagedUserRequest(limit, page, searchString)
        }

    suspend fun deleteUser(userId: String): DeleteUserResponse =
        client.delete("/admin/user/delete") {
            body = DeleteUsersRequest(listOf(userId))
        }

    suspend fun deleteUsers(userIds: List<String>): DeleteUserResponse =
        client.delete("/admin/user/delete") {
            body = DeleteUsersRequest(userIds)
        }

}