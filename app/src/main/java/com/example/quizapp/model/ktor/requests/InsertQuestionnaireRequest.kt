package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.mongodb.documents.questionnaire.MongoQuestionnaire
import kotlinx.serialization.Serializable

@Serializable
data class InsertQuestionnaireRequest(
    val mongoQuestionnaire: MongoQuestionnaire
)
