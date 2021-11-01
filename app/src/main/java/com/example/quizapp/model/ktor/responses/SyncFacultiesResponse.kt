package com.example.quizapp.model.ktor.responses

import com.example.quizapp.model.databases.mongodb.documents.faculty.MongoFaculty
import kotlinx.serialization.Serializable

@Serializable
data class SyncFacultiesResponse(
    val faculties: List<MongoFaculty>,
    val facultyIdsToDelete: List<String>
)
