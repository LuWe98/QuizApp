package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.mongodb.documents.MongoFaculty
import com.example.quizapp.model.ktor.ApiPaths.FacultyPaths
import com.example.quizapp.model.ktor.BackendRequest.*
import com.example.quizapp.model.ktor.BackendResponse.*
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FacultyApiImpl @Inject constructor(
    private val client: HttpClient
) : FacultyApi {

    override suspend fun getFacultySynchronizationData(localFacultyIdsWithTimeStamp: List<FacultyIdWithTimeStamp>) : SyncFacultiesResponse =
        client.post(FacultyPaths.SYNC) {
            body = SyncFacultiesRequest(localFacultyIdsWithTimeStamp)
        }

    override suspend fun insertFaculty(faculty: MongoFaculty) : InsertFacultyResponse =
        client.post(FacultyPaths.INSERT) {
            body = InsertFacultyRequest(faculty)
        }

    override suspend fun deleteFaculty(facultyId: String) : DeleteFacultyResponse =
        client.delete(FacultyPaths.DELETE) {
            body = DeleteFacultyRequest(facultyId)
        }

}