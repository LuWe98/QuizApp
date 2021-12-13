package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.databases.mongodb.documents.MongoFilledQuestionnaire
import kotlinx.serialization.Serializable

@Serializable
data class InsertFilledQuestionnaireRequest(
    val shouldBeIgnoredWhenAnotherIsPresent : Boolean,
    val mongoFilledQuestionnaire: MongoFilledQuestionnaire
)
