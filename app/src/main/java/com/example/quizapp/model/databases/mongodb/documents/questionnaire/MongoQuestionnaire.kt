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
    @BsonId var id : String = ObjectId().toHexString(),
    var title : String = "",
    var authorInfo : AuthorInfo,
    var questionnaireVisibility: QuestionnaireVisibility = QuestionnaireVisibility.PRIVATE,
    var facultyIds: List<String> = emptyList(),
    var courseOfStudiesIds: List<String> = emptyList(),
    var subject: String = "",
    var questions : List<MongoQuestion> = emptyList(),
    var lastModifiedTimestamp: Long = getTimeMillis()
) {
    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(MongoQuestionnaire::id)
    }
}