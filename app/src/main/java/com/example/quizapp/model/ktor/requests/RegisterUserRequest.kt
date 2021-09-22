package com.example.quizapp.model.ktor.requests

data class RegisterUserRequest(
    val email: String,
    val password: String,
    val courseOfStudies : String
)