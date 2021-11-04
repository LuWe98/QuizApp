package com.example.quizapp.model.databases.room.entities.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.room.entities.EntityMarker
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

//TODO -> Diese Entity ist dazu da, dass wenn man Antworten lokal gelöscht hat. Es wird hier nur eingetragen, wenn
//TODO -> es nicht synchronisiert werden konnte. Beim nächsten Syncen, werden die werte dann online gelöscht
@Entity(tableName = LocallyClearedQuestionnaire.TABLE_NAME)
@Parcelize
@Serializable
data class LocallyClearedQuestionnaire(
    @PrimaryKey
    @ColumnInfo(name = QUESTIONNAIRE_ID_COLUMN)
    val questionnaireId: String
) : EntityMarker {

    companion object {
        const val TABLE_NAME = "deletedFilledQuestionnairesTable"

        const val QUESTIONNAIRE_ID_COLUMN = "questionnaireId"
    }
}