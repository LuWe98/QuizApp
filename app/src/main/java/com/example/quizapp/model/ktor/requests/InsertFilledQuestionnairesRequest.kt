package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.mongodb.documents.filledquestionnaire.MongoFilledQuestionnaire
import kotlinx.serialization.Serializable

@Serializable
data class InsertFilledQuestionnairesRequest(
    val mongoFilledQuestionnaire: List<MongoFilledQuestionnaire>
)
