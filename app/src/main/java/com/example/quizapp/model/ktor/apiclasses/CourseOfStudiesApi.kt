package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.mongodb.documents.MongoCourseOfStudies
import com.example.quizapp.model.ktor.BackendResponse.*

interface CourseOfStudiesApi {

    suspend fun getCourseOfStudiesSynchronizationData(localCourseIfStudiesIdsWithTimeStamp: List<CourseOfStudiesIdWithTimeStamp>) : SyncCoursesOfStudiesResponse

    suspend fun insertCourseOfStudies(courseOfStudies: MongoCourseOfStudies) : InsertCourseOfStudiesResponse

    suspend fun deleteCourseOfStudies(courseOfStudiesId: String) : DeleteCourseOfStudiesResponse

}