package com.example.quizapp.model.mongo.userquestionnairedocument

data class MongoFilledQuestion(
    var questionId : String,
    var selectedAnswerIds : List<String> = emptyList()
)