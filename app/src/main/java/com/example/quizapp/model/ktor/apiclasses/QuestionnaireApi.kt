package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionnaireApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun getQuestionnaireOfUser(): List<MongoQuestionnaire> = client.get("/questionnaire/user")

}