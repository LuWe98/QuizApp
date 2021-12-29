package com.example.quizapp.model.databases.properties

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.example.quizapp.extensions.generateDiffItemCallback
import com.example.quizapp.model.databases.room.entities.Questionnaire
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class AuthorInfo(
    @ColumnInfo(name = Questionnaire.USER_ID_COLUMN)
    val userId: String,
    @ColumnInfo(name = Questionnaire.USER_NAME_COLUMN)
    val userName: String
) : Parcelable {

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(AuthorInfo::userId)
    }

}