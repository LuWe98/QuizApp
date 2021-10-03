package com.example.quizapp.model.mongo.userquestionnairedocument

data class MongoFilledQuestionnaire(
    var questionnaireId : String,
    var userId : String,
    var questions : List<MongoFilledQuestion> = emptyList()
)