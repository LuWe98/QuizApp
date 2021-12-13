package com.example.quizapp.model.databases.mongodb.documents

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Parcelize
@Serializable
data class MongoQuestion(
    @BsonId val id : String = ObjectId().toHexString(),
    val questionText : String,
    val isMultipleChoice: Boolean,
    val questionPosition : Int,
    val answers : List<MongoAnswer> = emptyList()
): Parcelable