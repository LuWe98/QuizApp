package com.example.quizapp.model.mongodb.documents.questionnairefilled

import kotlinx.serialization.Serializable

@Serializable
data class MongoFilledQuestion(
    var questionId : String,
    var selectedAnswerIds : List<String> = emptyList()
)