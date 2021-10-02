package com.example.quizapp.model.realm

import org.bson.types.ObjectId

data class MongoQuestion(
    var id : String = ObjectId().toString(),
    var questionText : String = "",
    var questionPosition : Int = 0,
    var answers : List<MongoAnswer> = emptyList()
)