package com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire

data class MongoFilledQuestionnaire(
    var questionnaireId : String,
    var userId : String,
    var questions : List<MongoFilledQuestion> = emptyList()
)