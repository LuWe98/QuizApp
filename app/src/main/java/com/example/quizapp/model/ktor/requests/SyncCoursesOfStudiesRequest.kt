package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import kotlinx.serialization.Serializable

@Serializable
data class SyncCoursesOfStudiesRequest(
    val localCourseOfStudiesWithTimeStamp: List<CourseOfStudiesIdWithTimeStamp>
)