package com.example.quizapp.model.databases.room.entities

import androidx.room.*
import com.example.quizapp.extensions.generateDiffItemCallback
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
    val id: String = ObjectId().toHexString(),
    @ColumnInfo(name = QUESTIONNAIRE_ID_COLUMN)
    val questionnaireId: String = Questionnaire.UNKNOWN_QUESTIONNAIRE_ID,
    @ColumnInfo(name = TEXT_COLUMN)
    val questionText: String = EMPTY_QUESTION_TEXT,
    @ColumnInfo(name = IS_MULTIPLE_CHOICE_COLUMN)
    val isMultipleChoice: Boolean = true,
    @ColumnInfo(name = POSITION_COLUMN)
    val questionPosition: Int = 0
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(Question::id)

        const val TABLE_NAME = "questionTable"

        const val ID_COLUMN = "id"
        const val QUESTIONNAIRE_ID_COLUMN = "questionnaireId"
        const val TEXT_COLUMN = "questionText"
        const val IS_MULTIPLE_CHOICE_COLUMN = "isMultipleChoice"
        const val POSITION_COLUMN = "questionPosition"

        const val EMPTY_QUESTION_TEXT = ""
    }

}