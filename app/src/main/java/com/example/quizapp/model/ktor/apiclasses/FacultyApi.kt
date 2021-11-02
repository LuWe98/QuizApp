package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
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

    suspend fun getFacultySynchronizationData(localFacultyIdsWithTimeStamp: List<FacultyIdWithTimeStamp>) : SyncFacultiesResponse =
        client.post("/faculty/sync") {
            body = SyncFacultiesRequest(localFacultyIdsWithTimeStamp)
        }

}