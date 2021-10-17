package com.example.quizapp.model.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class GetPagedUserRequest(
    val limit: Int ,
    val page: Int,
    val searchString: String
)