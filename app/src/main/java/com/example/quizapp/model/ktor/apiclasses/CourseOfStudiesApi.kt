package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.ktor.requests.SyncCoursesOfStudiesRequest
import com.example.quizapp.model.ktor.requests.SyncFacultiesRequest
import com.example.quizapp.model.ktor.responses.SyncCoursesOfStudiesResponse
import com.example.quizapp.model.ktor.responses.SyncFacultiesResponse
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseOfStudiesApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun getCourseOfStudiesSynchronizationData(localCourseIfStudiesIdsWithTimeStamp: List<CourseOfStudiesIdWithTimeStamp>) : SyncCoursesOfStudiesResponse =
        client.post("/courseOfStudies/sync") {
            body = SyncCoursesOfStudiesRequest(localCourseIfStudiesIdsWithTimeStamp)
        }

}