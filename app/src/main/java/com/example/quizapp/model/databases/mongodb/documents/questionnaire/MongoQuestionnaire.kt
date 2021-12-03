package com.example.quizapp.model.databases.mongodb.documents.questionnaire

import com.example.quizapp.extensions.generateDiffItemCallback
import com.example.quizapp.model.databases.QuestionnaireVisibility
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoQuestionnaire(
    @BsonId val id : String = ObjectId().toHexString(),
    val title : String,
    val authorInfo : AuthorInfo,
    val questionnaireVisibility: QuestionnaireVisibility = QuestionnaireVisibility.PRIVATE,
    val facultyIds: List<String> = emptyList(),
    val courseOfStudiesIds: List<String> = emptyList(),
    val subject: String,
    val questions : List<MongoQuestion> = emptyList(),
    val lastModifiedTimestamp: Long = getTimeMillis()
) {
    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(MongoQuestionnaire::id)
    }
}