package com.example.quizapp.model.ktor.requests

data class LoginUserRequest(
    val email: String,
    val password: String
)
