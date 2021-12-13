package com.example.quizapp.model.databases.mongodb.documents.user

import android.os.Parcelable
import com.example.quizapp.extensions.generateDiffItemCallback
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
@Parcelize
data class User(
    @BsonId val id: String = ObjectId().toHexString(),
    val userName: String,
    val password: String = "",
    var role: Role = Role.USER,
    var lastModifiedTimestamp : Long
) : Parcelable {

    val isNotEmpty get() = id.isNotEmpty() && userName.isNotEmpty() && password.isNotEmpty()

    val isEmpty get() = !isNotEmpty

    val asAuthorInfo get() = AuthorInfo(id, userName)

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(User::id)
    }
}