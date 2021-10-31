package com.example.quizapp.model.mongodb.documents.faculty

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoSubject(
    @BsonId var id: String = ObjectId().toString(),
    var abbreviation: String,
    var name: String
)