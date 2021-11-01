package com.example.quizapp.model.ktor.responses

import com.example.quizapp.model.databases.mongodb.documents.user.Role
import kotlinx.serialization.Serializable

@Serializable
data class SyncUserDataResponse(
    val role: Role? = null,
    val lastModifiedTimestamp: Long? = null,
    val responseType: SyncUserDataResponseType
) {
    enum class SyncUserDataResponseType {
        DATA_UP_TO_DATE,
        DATA_CHANGED
    }
}