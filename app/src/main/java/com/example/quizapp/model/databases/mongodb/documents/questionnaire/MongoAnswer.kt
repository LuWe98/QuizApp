package com.example.quizapp.model.databases.mongodb.documents.questionnaire

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Parcelize
@Serializable
data class MongoAnswer(
    @BsonId var id : String = ObjectId().toHexString(),
    var answerText : String = "",
    var answerPosition : Int = 0,
    var isAnswerCorrect : Boolean = false
): Parcelable