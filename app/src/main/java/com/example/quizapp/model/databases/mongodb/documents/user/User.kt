package com.example.quizapp.model.databases.mongodb.documents.user

import android.os.Parcelable
import com.example.quizapp.utils.DiffCallbackUtil
import io.ktor.client.features.auth.providers.*
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import okhttp3.Credentials
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
@Parcelize
data class User(
    @BsonId var id: String = ObjectId().toString(),
    var userName: String,
    var password: String,
    var role: Role,
    var lastModifiedTimestamp : Long
) : Parcelable {

    val isNotEmpty get() = id.isNotEmpty() && userName.isNotEmpty() && password.isNotEmpty()

    val isEmpty get() = !isNotEmpty

    val asAuthorInfo get() = AuthorInfo(id, userName)

    val asBasicAuthCredentials get() = BasicAuthCredentials(userName, password)

    val asBasicCredentials get() = Credentials.basic(userName, password, Charsets.UTF_8)

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<User> { old, new -> old.id == new.id }
    }
}