package com.example.quizapp.model.ktor.requests.user

data class RegisterUserRequest(
    val userName: String,
    val password: String,
    val courseOfStudies : String
)