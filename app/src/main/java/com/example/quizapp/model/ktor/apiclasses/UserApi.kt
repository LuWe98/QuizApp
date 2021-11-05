package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.ktor.requests.*
import com.example.quizapp.model.ktor.responses.*
import com.quizappbackend.routing.ApiPaths.*
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun loginUser(newUserName: String, password: String): LoginUserResponse =
        client.post(UserPaths.LOGIN) {
            body = LoginUserRequest(newUserName, password)
        }

    suspend fun registerUser(newUserName: String, password: String): RegisterUserResponse =
        client.post(UserPaths.REGISTER) {
            body = RegisterUserRequest(newUserName, password)
        }

    suspend fun updateUsername(newUserName: String): UpdateUserResponse =
        client.post(UserPaths.UPDATE_USERNAME) {
            body = UpdateUserNameRequest(newUserName)
        }

    suspend fun deleteSelf(): DeleteUserResponse =
        client.delete(UserPaths.DELETE_SELF)

    suspend fun syncUserData(userId: String) : SyncUserDataResponse =
        client.post(UserPaths.SYNC){
            body = SyncUserDataRequest(userId)
        }

    suspend fun refreshJwtToken(userName: String, password: String) : RefreshJwtTokenResponse =
        client.post(UserPaths.REFRESH_TOKEN) {
            body = RefreshJwtTokenRequest(userName, password)
        }


    //ADMIN
    suspend fun updateUserRole(userId: String, newRole: Role): UpdateUserResponse =
        client.post(UserPaths.UPDATE_ROLE) {
            body = UpdateUserRoleRequest(userId, newRole)
        }

    suspend fun getPagedUsers(limit: Int, page: Int, searchString: String) : List<User> =
        client.post(UserPaths.USERS_PAGED) {
            body = GetPagedUserRequest(limit, page, searchString)
        }

    suspend fun deleteUser(userId: String): DeleteUserResponse = deleteUsers(listOf(userId))

    suspend fun deleteUsers(userIds: List<String>): DeleteUserResponse =
        client.delete(UserPaths.DELETE_USER) {
            body = DeleteUsersRequest(userIds)
        }
}