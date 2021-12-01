package com.example.quizapp.model.databases.mongodb.documents.questionnaire

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Parcelize
@Serializable
data class MongoQuestion(
    @BsonId var id : String = ObjectId().toHexString(),
    var questionText : String = "",
    var isMultipleChoice: Boolean = true,
    var questionPosition : Int = 0,
    var answers : List<MongoAnswer> = emptyList()
): Parcelable