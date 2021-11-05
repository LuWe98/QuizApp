package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteCourseOfStudiesRequest(
    val courseOfStudiesId: String
)
