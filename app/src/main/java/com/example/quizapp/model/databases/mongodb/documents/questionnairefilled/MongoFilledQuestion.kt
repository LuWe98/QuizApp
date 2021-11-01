package com.example.quizapp.model.databases.mongodb.documents.questionnairefilled

import kotlinx.serialization.Serializable

@Serializable
data class MongoFilledQuestion(
    var questionId : String,
    var selectedAnswerIds : List<String> = emptyList()
)