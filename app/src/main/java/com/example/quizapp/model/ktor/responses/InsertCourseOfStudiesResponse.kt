package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class InsertCourseOfStudiesResponse(
    val responseType: InsertCourseOfStudiesResponseType
) {
    enum class  InsertCourseOfStudiesResponseType {
        SUCCESSFUL,
        NOT_ACKNOWLEDGED
    }
}
