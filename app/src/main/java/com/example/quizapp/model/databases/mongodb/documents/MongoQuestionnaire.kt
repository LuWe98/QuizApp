package com.example.quizapp.model.databases.mongodb.documents

import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.databases.properties.AuthorInfo
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoQuestionnaire(
    @BsonId val id : String = ObjectId().toHexString(),
    val title : String,
    val authorInfo : AuthorInfo,
    val visibility: QuestionnaireVisibility = QuestionnaireVisibility.PRIVATE,
    val facultyIds: List<String> = emptyList(),
    val courseOfStudiesIds: List<String> = emptyList(),
    val subject: String,
    val questionCount: Int,
    val questions : List<MongoQuestion> = emptyList(),
    val lastModifiedTimestamp: Long = getTimeMillis()
)