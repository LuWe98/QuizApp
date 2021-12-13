package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.mongodb.documents.MongoFaculty
import com.example.quizapp.model.ktor.requests.DeleteFacultyRequest
import com.example.quizapp.model.ktor.requests.InsertFacultyRequest
import com.example.quizapp.model.ktor.requests.SyncFacultiesRequest
import com.example.quizapp.model.ktor.responses.DeleteFacultyResponse
import com.example.quizapp.model.ktor.responses.InsertFacultyResponse
import com.example.quizapp.model.ktor.responses.SyncFacultiesResponse
import com.example.quizapp.model.ktor.ApiPaths.FacultyPaths
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FacultyApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun getFacultySynchronizationData(localFacultyIdsWithTimeStamp: List<FacultyIdWithTimeStamp>) : SyncFacultiesResponse =
        client.post(FacultyPaths.SYNC) {
            body = SyncFacultiesRequest(localFacultyIdsWithTimeStamp)
        }

    suspend fun insertFaculty(faculty: MongoFaculty) : InsertFacultyResponse =
        client.post(FacultyPaths.INSERT) {
            body = InsertFacultyRequest(faculty)
        }

    suspend fun deleteFaculty(facultyId: String) : DeleteFacultyResponse =
        client.post(FacultyPaths.DELETE) {
            body = DeleteFacultyRequest(facultyId)
        }
}