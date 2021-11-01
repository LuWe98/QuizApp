package com.example.quizapp.model.ktor.responses

import com.example.quizapp.model.databases.mongodb.documents.faculty.MongoCourseOfStudies
import kotlinx.serialization.Serializable

@Serializable
data class SyncCoursesOfStudiesResponse(
    val coursesOfStudies: List<MongoCourseOfStudies>,
    val courseOfStudiesIdsToDelete: List<String>
)