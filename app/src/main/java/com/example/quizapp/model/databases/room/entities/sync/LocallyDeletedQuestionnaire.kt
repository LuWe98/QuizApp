package com.example.quizapp.model.databases.room.entities.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.room.entities.EntityMarker
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

//TODO -> Diese Entity ist dazu da, dass wenn man fragebögen lokal löscht und kein inet hat, dass man bei einem neustart diese Fragebögen dann online nachlöschen kann
//TODO -> Fragebögen, welche ihre ID hier drin haben, werden auch nicht online nachgeladen, wenn man die App startet
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