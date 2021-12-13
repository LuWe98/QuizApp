package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.mongodb.documents.MongoFilledQuestionnaire
import kotlinx.serialization.Serializable

@Serializable
data class InsertFilledQuestionnairesRequest(
    val mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>
)
