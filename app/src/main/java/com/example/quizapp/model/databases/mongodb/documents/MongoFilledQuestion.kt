package com.example.quizapp.model.databases.mongodb.documents

import kotlinx.serialization.Serializable

@Serializable
data class MongoFilledQuestion(
    val questionId : String,
    val selectedAnswerIds : List<String> = emptyList()
)