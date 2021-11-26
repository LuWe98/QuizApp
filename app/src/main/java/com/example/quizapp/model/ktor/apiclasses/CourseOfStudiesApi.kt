package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.mongodb.documents.faculty.MongoCourseOfStudies
import com.example.quizapp.model.ktor.requests.DeleteCourseOfStudiesRequest
import com.example.quizapp.model.ktor.requests.InsertCourseOfStudiesRequest
import com.example.quizapp.model.ktor.requests.SyncCoursesOfStudiesRequest
import com.example.quizapp.model.ktor.responses.DeleteCourseOfStudiesResponse
import com.example.quizapp.model.ktor.responses.InsertCourseOfStudiesResponse
import com.example.quizapp.model.ktor.responses.SyncCoursesOfStudiesResponse
import com.example.quizapp.model.ktor.ApiPaths.CourseOfStudiesPaths
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseOfStudiesApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun getCourseOfStudiesSynchronizationData(localCourseIfStudiesIdsWithTimeStamp: List<CourseOfStudiesIdWithTimeStamp>) : SyncCoursesOfStudiesResponse =
        client.post(CourseOfStudiesPaths.SYNC) {
            body = SyncCoursesOfStudiesRequest(localCourseIfStudiesIdsWithTimeStamp)
        }

    suspend fun insertCourseOfStudies(courseOfStudies: MongoCourseOfStudies) : InsertCourseOfStudiesResponse =
        client.post(CourseOfStudiesPaths.INSERT) {
            body = InsertCourseOfStudiesRequest(courseOfStudies)
        }

    suspend fun deleteCourseOfStudies(courseOfStudiesId: String) : DeleteCourseOfStudiesResponse =
        client.post(CourseOfStudiesPaths.DELETE) {
            body = DeleteCourseOfStudiesRequest(courseOfStudiesId)
        }

}