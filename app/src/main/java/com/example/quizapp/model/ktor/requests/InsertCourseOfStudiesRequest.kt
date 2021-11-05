package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.mongodb.documents.faculty.MongoCourseOfStudies
import kotlinx.serialization.Serializable

@Serializable
data class InsertCourseOfStudiesRequest(
    val courseOfStudies: MongoCourseOfStudies
)
