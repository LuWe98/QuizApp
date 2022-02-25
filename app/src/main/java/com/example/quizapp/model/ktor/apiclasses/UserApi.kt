package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.datastore.datawrappers.ManageUsersOrderBy
import com.example.quizapp.model.ktor.BackendResponse.*

interface UserApi {

    suspend fun loginUser(newUserName: String, password: String): LoginUserResponse

    suspend fun registerUser(newUserName: String, password: String): RegisterUserResponse

    suspend fun deleteSelf(): DeleteUserResponse

    suspend fun syncUserData(): SyncUserDataResponse

    suspend fun refreshJwtToken(userName: String, password: String): RefreshJwtTokenResponse

    suspend fun getPagedAuthors(limit: Int, page: Int, searchString: String): List<AuthorInfo>

    suspend fun updateUserPassword(newPassword: String): ChangePasswordResponse

    suspend fun updateUserCanShareQuestionnaireWith(): UpdateUserCanShareQuestionnaireWithResponse

    suspend fun updateUserRole(userId: String, newRole: Role): UpdateUserResponse

    suspend fun getPagedUsersAdmin(limit: Int, page: Int, searchString: String, roles: Set<Role>, orderBy: ManageUsersOrderBy, ascending: Boolean): List<User>

    suspend fun deleteUser(userId: String): DeleteUserResponse

    suspend fun createUser(userName: String, password: String, role: Role): CreateUserResponse

}