package com.example.quizapp.model.databases.mongodb.documents

import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoFaculty(
    @BsonId val id: String = ObjectId().toHexString(),
    val abbreviation: String,
    val name: String,
    val lastModifiedTimestamp : Long = getTimeMillis()
)