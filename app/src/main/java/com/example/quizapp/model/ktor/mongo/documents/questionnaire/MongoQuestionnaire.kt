package com.example.quizapp.model.ktor.mongo.documents.questionnaire

import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoQuestionnaire(
    @BsonId var id : String = ObjectId().toString(),
    var title : String = "",
    var author : String = "",
    var lastModifiedTimestamp: Long = getTimeMillis(),
    var courseOfStudies : String = "",
    var questions : List<MongoQuestion> = emptyList()
)