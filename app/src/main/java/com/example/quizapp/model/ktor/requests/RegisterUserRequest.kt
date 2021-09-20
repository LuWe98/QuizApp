package com.example.quizapp.model.ktor.requests

data class RegisterUserRequest(
    val email: String,
    val userName: String,
    val password: String
)