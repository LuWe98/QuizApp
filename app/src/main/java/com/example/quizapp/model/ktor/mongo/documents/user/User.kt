package com.example.quizapp.model.ktor.mongo.documents.user

import android.os.Parcelable
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Parcelize
data class User(
    @BsonId val id: String = ObjectId().toString(),
    var userName: String,
    var password: String,
    var lastModifiedTimestamp : Long = getTimeMillis()
) : Parcelable