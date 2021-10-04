package com.example.quizapp.model.ktor.mongo.documents.questionnaire

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoAnswer(
    @BsonId var id : String = ObjectId().toString(),
    var answerText : String = "",
    var answerPosition : Int = 0,
    var isAnswerCorrect : Boolean = false
)