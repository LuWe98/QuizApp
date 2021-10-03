package com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire

data class MongoFilledQuestion(
    var questionId : String,
    var selectedAnswerIds : List<String> = emptyList()
)