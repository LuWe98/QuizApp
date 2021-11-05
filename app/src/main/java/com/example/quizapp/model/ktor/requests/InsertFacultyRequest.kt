package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.mongodb.documents.faculty.MongoFaculty
import kotlinx.serialization.Serializable

@Serializable
data class InsertFacultyRequest(
    val faculty: MongoFaculty
)
