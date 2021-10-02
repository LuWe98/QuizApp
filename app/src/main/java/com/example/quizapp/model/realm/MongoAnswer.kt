package com.example.quizapp.model.realm

import org.bson.types.ObjectId

data class MongoAnswer(
    var id : String = ObjectId().toString(),
    var answerText : String = "",
    var answerPosition : Int = 0,
    var isAnswerCorrect : Boolean = false
)