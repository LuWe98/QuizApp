package com.example.quizapp.model.databases.room.entities.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.room.entities.EntityMarker
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

//TODO -> Diese Entity ist dazu da, wenn man Antworten zu einem Fragebogen gegeben hat, dass diese auch hochgeladen werden, auch wenn man die App verlässt
@Parcelize
@Serializable
@Entity(tableName = LocallyAnsweredQuestionnaire.TABLE_NAME)
class LocallyAnsweredQuestionnaire(
    @PrimaryKey
    @ColumnInfo(name = QUESTIONNAIRE_ID_COLUMN)
    val questionnaireId: String
) : EntityMarker {

    companion object {
        const val TABLE_NAME = "locallyAnsweredQuestionnairesTable"

        const val QUESTIONNAIRE_ID_COLUMN = "questionnaireId"
    }

}