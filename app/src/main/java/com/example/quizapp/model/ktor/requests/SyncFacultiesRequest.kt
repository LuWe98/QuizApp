package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import kotlinx.serialization.Serializable

@Serializable
data class SyncFacultiesRequest(
    val localFacultyIdsWithTimeStamp: List<FacultyIdWithTimeStamp>
)
