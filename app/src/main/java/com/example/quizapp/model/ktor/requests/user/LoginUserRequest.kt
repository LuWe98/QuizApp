package com.example.quizapp.model.ktor.requests.user

data class LoginUserRequest(
    val userName: String,
    val password: String
)
