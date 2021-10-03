package com.example.quizapp.model.mongo.questionnairedocument

import org.bson.types.ObjectId

data class MongoQuestionnaire(
    var id : String = ObjectId().toString(),
    var title : String = "",
    var author : String = "",
    var courseOfStudies : String = "",
    var questions : List<MongoQuestion> = emptyList()
)