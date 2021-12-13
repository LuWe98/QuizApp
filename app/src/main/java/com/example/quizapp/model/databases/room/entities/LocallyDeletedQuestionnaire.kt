package com.example.quizapp.model.databases.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Entity(tableName = LocallyDeletedQuestionnaire.TABLE_NAME)
@Parcelize
@Serializable
data class LocallyDeletedQuestionnaire(
    @PrimaryKey
    @ColumnInfo(name = QUESTIONNAIRE_ID_COLUMN)
    val questionnaireId: String,
    @ColumnInfo(name = IS_USER_OWNER_COLUMN)
    val isUserOwner : Boolean
) : EntityMarker {

    companion object{
        fun asOwner(questionnaireId: String) = LocallyDeletedQuestionnaire(questionnaireId, true)
        fun notAsOwner(questionnaireId: String) = LocallyDeletedQuestionnaire(questionnaireId, false)

        const val TABLE_NAME = "deletedQuestionnairesTable"

        const val QUESTIONNAIRE_ID_COLUMN = "questionnaireId"
        const val IS_USER_OWNER_COLUMN = "isUserOwner"
    }

}