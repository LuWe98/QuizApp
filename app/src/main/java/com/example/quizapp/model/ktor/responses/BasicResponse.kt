package com.example.quizapp.model.ktor.responses

data class BasicResponse<T>(
    val successful: Boolean,
    val message: String,
    val data : T? = null
)