package com.example.quizapp.model.databases.room.entities.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.room.entities.EntityMarker
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = LocallyFilledQuestionnaireToUpload.TABLE_NAME)
class LocallyFilledQuestionnaireToUpload(
    @PrimaryKey
    @ColumnInfo(name = QUESTIONNAIRE_ID_COLUMN)
    val questionnaireId: String
) : EntityMarker {

    companion object {
        const val TABLE_NAME = "locallyFilledQuestionnaireToUploadTable"

        const val QUESTIONNAIRE_ID_COLUMN = "questionnaireId"
    }

}