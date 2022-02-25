package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.mongodb.documents.MongoCourseOfStudies
import com.example.quizapp.model.ktor.ApiPaths.CourseOfStudiesPaths
import com.example.quizapp.model.ktor.BackendRequest.*
import com.example.quizapp.model.ktor.BackendResponse.*
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseOfStudiesApiImpl @Inject constructor(
    private val client: HttpClient
) : CourseOfStudiesApi {

    override suspend fun getCourseOfStudiesSynchronizationData(localCourseIfStudiesIdsWithTimeStamp: List<CourseOfStudiesIdWithTimeStamp>) : SyncCoursesOfStudiesResponse =
        client.post(CourseOfStudiesPaths.SYNC) {
            body = SyncCoursesOfStudiesRequest(localCourseIfStudiesIdsWithTimeStamp)
        }

    override suspend fun insertCourseOfStudies(courseOfStudies: MongoCourseOfStudies) : InsertCourseOfStudiesResponse =
        client.post(CourseOfStudiesPaths.INSERT) {
            body = InsertCourseOfStudiesRequest(courseOfStudies)
        }

    override suspend fun deleteCourseOfStudies(courseOfStudiesId: String) : DeleteCourseOfStudiesResponse =
        client.delete(CourseOfStudiesPaths.DELETE) {
            body = DeleteCourseOfStudiesRequest(courseOfStudiesId)
        }

}