package com.example.quizapp.model.databases.room.entities.questionnaire

import androidx.room.*
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.utils.DiffCallbackUtil
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(
    tableName = Question.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Questionnaire::class,
            parentColumns = [Questionnaire.ID_COLUMN],
            childColumns = [Question.QUESTIONNAIRE_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [Question.QUESTIONNAIRE_ID_COLUMN])
    ]
)
@Parcelize
data class Question(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    var id: String = ObjectId().toHexString(),
    @ColumnInfo(name = QUESTIONNAIRE_ID_COLUMN)
    var questionnaireId: String,
    @ColumnInfo(name = TEXT_COLUMN)
    var questionText: String = "",
    @ColumnInfo(name = IS_MULTIPLE_CHOICE_COLUMN)
    var isMultipleChoice: Boolean = true,
    @ColumnInfo(name = POSITION_COLUMN)
    var questionPosition: Int = 0
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<Question> { old, new ->  old.id == new.id}

        const val TABLE_NAME = "questionTable"

        const val ID_COLUMN = "id"
        const val QUESTIONNAIRE_ID_COLUMN = "questionnaireId"
        const val TEXT_COLUMN = "questionText"
        const val IS_MULTIPLE_CHOICE_COLUMN = "isMultipleChoice"
        const val POSITION_COLUMN = "questionPosition"
    }

}