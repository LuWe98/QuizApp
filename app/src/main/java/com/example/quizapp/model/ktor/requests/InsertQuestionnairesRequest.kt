package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.mongodb.documents.MongoQuestionnaire
import kotlinx.serialization.Serializable

@Serializable
data class InsertQuestionnairesRequest(
    val mongoQuestionnaires: List<MongoQuestionnaire>
)
