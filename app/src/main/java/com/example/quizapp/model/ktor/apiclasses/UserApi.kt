package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.datastore.datawrappers.ManageUsersOrderBy
import com.example.quizapp.model.ktor.ApiPaths.UserPaths
import com.example.quizapp.model.ktor.BackendRequest.*
import com.example.quizapp.model.ktor.BackendResponse.*
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

    suspend fun syncUserData(userId: String): SyncUserDataResponse =
        client.post(UserPaths.SYNC) {
            body = SyncUserDataRequest(userId)
        }

    suspend fun refreshJwtToken(userName: String, password: String): RefreshJwtTokenResponse =
        client.post(UserPaths.REFRESH_TOKEN) {
            body = RefreshJwtTokenRequest(userName, password)
        }

    suspend fun getPagedAuthors(limit: Int, page: Int, searchString: String): List<AuthorInfo> =
        client.post(UserPaths.AUTHORS_PAGED) {
            body = GetPagedAuthorsRequest(limit, page, searchString)
        }

    suspend fun updateUserPassword(newPassword: String): ChangePasswordResponse =
        client.post(UserPaths.UPDATE_PASSWORD) {
            body = ChangePasswordRequest(newPassword)
        }


    //ADMIN
    suspend fun updateUserRole(userId: String, newRole: Role): UpdateUserResponse =
        client.post(UserPaths.UPDATE_ROLE) {
            body = UpdateUserRoleRequest(userId, newRole)
        }

    suspend fun getPagedUsersAdmin(limit: Int, page: Int, searchString: String, roles: Set<Role>, orderBy: ManageUsersOrderBy, ascending: Boolean): List<User> =
        client.post(UserPaths.USERS_PAGED_ADMIN) {
            body = GetPagedUserAdminRequest(limit, page, searchString, roles, orderBy, ascending)
        }

    suspend fun deleteUser(userId: String): DeleteUserResponse =
        client.delete(UserPaths.DELETE_USER) {
            body = DeleteUserRequest(userId)
        }

    suspend fun createUser(userName: String, password: String, role: Role): CreateUserResponse =
        client.post(UserPaths.CREATE) {
            body = CreateUserRequest(userName, password, role)
        }

}