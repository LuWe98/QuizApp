package com.example.quizapp.model.ktor.mongo.documents.questionnaire

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class AuthorInfo(
    val userId: String,
    val userName: String
) : Parcelable