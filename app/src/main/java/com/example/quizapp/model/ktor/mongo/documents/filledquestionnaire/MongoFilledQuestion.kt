package com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire

import kotlinx.serialization.Serializable

@Serializable
data class MongoFilledQuestion(
    var questionId : String,
    var selectedAnswerIds : List<String> = emptyList()
)