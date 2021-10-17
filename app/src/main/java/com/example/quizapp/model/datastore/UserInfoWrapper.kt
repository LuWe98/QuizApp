package com.example.quizapp.model.datastore

import com.example.quizapp.model.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.mongodb.documents.user.Role

data class UserInfoWrapper(
    val id: String,
    val name: String,
    val password: String,
    val role: Role
) {
    val isNotEmpty get() = id.isNotEmpty() && name.isNotEmpty() && password.isNotEmpty()

    fun asAuthorInfo() = AuthorInfo(id, name)
}
