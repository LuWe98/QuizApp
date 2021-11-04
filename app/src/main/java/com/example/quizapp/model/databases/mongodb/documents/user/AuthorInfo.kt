package com.example.quizapp.model.databases.mongodb.documents.user

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class AuthorInfo(
    @ColumnInfo(name = Questionnaire.USER_ID_COLUMN)
    val userId: String,
    @ColumnInfo(name = Questionnaire.USER_NAME_COLUMN)
    val userName: String
) : Parcelable