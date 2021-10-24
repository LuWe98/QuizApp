package com.example.quizapp.model.mongodb.documents.questionnaire

import com.example.quizapp.model.mongodb.documents.user.AuthorInfo
import com.example.quizapp.utils.DiffUtilHelper
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoQuestionnaire(
    @BsonId var id : String = ObjectId().toString(),
    var title : String = "",
    var authorInfo : AuthorInfo,
    var courseOfStudies : String = "",
    var subject: String = "",
    var questions : List<MongoQuestion> = emptyList(),
    var lastModifiedTimestamp: Long = getTimeMillis()
) {
    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<MongoQuestionnaire> { old, new -> old.id == new.id}
    }
}