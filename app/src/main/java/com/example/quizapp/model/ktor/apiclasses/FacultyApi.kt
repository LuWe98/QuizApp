package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.requests.SyncFacultiesRequest
import com.example.quizapp.model.ktor.responses.SyncFacultiesResponse
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FacultyApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun testFacultyStuff(locallyPresentFacultyIds: List<String>, locallyPresentCourseOfStudiesIds: List<String>) : SyncFacultiesResponse =
        client.get("/faculty/sync") {
            //TODO -> Timestamp noch mit einbeziehen!
            body = SyncFacultiesRequest(locallyPresentFacultyIds, locallyPresentCourseOfStudiesIds)
        }

}