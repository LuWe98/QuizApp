package com.example.quizapp.model.databases.dto

import kotlinx.serialization.Serializable

@Serializable
data class CourseOfStudiesIdWithTimeStamp(
    val courseOfStudiesId: String,
    val lastModifiedTimeStamp: Long
)