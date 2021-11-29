package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetPagedAuthorsRequest(
    val limit: Int,
    val page: Int,
    val searchString: String
)