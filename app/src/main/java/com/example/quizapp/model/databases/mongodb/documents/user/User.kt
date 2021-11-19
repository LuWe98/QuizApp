package com.example.quizapp.model.databases.mongodb.documents.user

import android.os.Parcelable
import com.example.quizapp.utils.DiffCallbackUtil
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
@Parcelize
data class User(
    @BsonId var id: String = ObjectId().toHexString(),
    var userName: String,
    var password: String,
    var role: Role,
    var lastModifiedTimestamp : Long
) : Parcelable {

    val isNotEmpty get() = id.isNotEmpty() && userName.isNotEmpty() && password.isNotEmpty()

    val isEmpty get() = !isNotEmpty

    val asAuthorInfo get() = AuthorInfo(id, userName)

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<User> { old, new -> old.id == new.id }
    }
}