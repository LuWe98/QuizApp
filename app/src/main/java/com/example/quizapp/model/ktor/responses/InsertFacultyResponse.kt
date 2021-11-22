package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class InsertFacultyResponse(
    val responseType: InsertFacultyResponseType
) {
    enum class  InsertFacultyResponseType {
        SUCCESSFUL,
        NOT_ACKNOWLEDGED
    }
}
