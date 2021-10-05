package com.example.quizapp.model.ktor.apiclasses

import io.ktor.client.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilledQuestionnaireApi @Inject constructor(
    private val client: HttpClient
)  {

}