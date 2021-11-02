package com.example.quizapp.model.databases.dto

import kotlinx.serialization.Serializable

@Serializable
data class FacultyIdWithTimeStamp(
    val facultyId: String,
    val lastModifiedTimestamp: Long
)