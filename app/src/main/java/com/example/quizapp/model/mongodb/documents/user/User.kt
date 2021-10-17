package com.example.quizapp.model.mongodb.documents.user

import android.os.Parcelable
import com.example.quizapp.utils.DiffUtilHelper
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
@Parcelize
data class User(
    @BsonId val id: String = ObjectId().toString(),
    var userName: String,
    var password: String,
    var role: Role,
    var lastModifiedTimestamp : Long = getTimeMillis()
) : Parcelable {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<User> { old, new -> old.id == new.id }
    }

}