package com.example.quizapp.model.databases.mongodb.documents.questionnaire

import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.utils.DiffCallbackUtil
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoQuestionnaire(
    @BsonId var id : String = ObjectId().toString(),
    var title : String = "",
    var authorInfo : AuthorInfo,
    var questionnaireVisibility: QuestionnaireVisibility = QuestionnaireVisibility.PRIVATE,
    var faculty: String = "",
    var courseOfStudies : String = "",
    var subject: String = "",
    var questions : List<MongoQuestion> = emptyList(),
    var lastModifiedTimestamp: Long = getTimeMillis()
) {
    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<MongoQuestionnaire> { old, new -> old.id == new.id}
    }
}