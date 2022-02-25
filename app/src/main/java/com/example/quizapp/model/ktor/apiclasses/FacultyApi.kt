package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.mongodb.documents.MongoFaculty
import com.example.quizapp.model.ktor.BackendResponse.*

interface FacultyApi {

    suspend fun getFacultySynchronizationData(localFacultyIdsWithTimeStamp: List<FacultyIdWithTimeStamp>) : SyncFacultiesResponse

    suspend fun insertFaculty(faculty: MongoFaculty) : InsertFacultyResponse

    suspend fun deleteFaculty(facultyId: String) : DeleteFacultyResponse

}