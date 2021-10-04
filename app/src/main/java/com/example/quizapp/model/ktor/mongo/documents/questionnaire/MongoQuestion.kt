package com.example.quizapp.model.ktor.mongo.documents.questionnaire

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoQuestion(
    @BsonId var id : String = ObjectId().toString(),
    var questionText : String = "",
    var isMultipleChoice: Boolean = true,
    var questionPosition : Int = 0,
    var answers : List<MongoAnswer> = emptyList()
)