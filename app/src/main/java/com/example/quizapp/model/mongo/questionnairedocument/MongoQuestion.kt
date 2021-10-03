package com.example.quizapp.model.mongo.questionnairedocument

import org.bson.types.ObjectId

data class MongoQuestion(
    var id : String = ObjectId().toString(),
    var questionText : String = "",
    var isMultipleChoice: Boolean = true,
    var questionPosition : Int = 0,
    var answers : List<MongoAnswer> = emptyList()
)