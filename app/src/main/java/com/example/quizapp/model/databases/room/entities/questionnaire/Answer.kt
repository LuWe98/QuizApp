package com.example.quizapp.model.databases.room.entities.questionnaire

import androidx.room.*
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.utils.DiffCallbackUtil
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(
    tableName = Answer.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = [Question.ID_COLUMN],
            childColumns = [Answer.QUESTION_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [Answer.QUESTION_ID_COLUMN])
    ]
)
@Parcelize
data class Answer(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    var id: String = ObjectId().toHexString(),
    @ColumnInfo(name = QUESTION_ID_COLUMN)
    var questionId: String = "",
    @ColumnInfo(name = TEXT_COLUMN)
    var answerText: String = "",
    @ColumnInfo(name = IS_ANSWER_CORRECT_COLUMN)
    var isAnswerCorrect: Boolean = false,
    @ColumnInfo(name = IS_ANSWER_SELECTED_COLUMN)
    var isAnswerSelected: Boolean = false,
    @ColumnInfo(name = POSITION_COLUMN)
    var answerPosition: Int = 0
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<Answer> { old, new ->  old.id == new.id}

        const val TABLE_NAME = "answerTable"

        const val ID_COLUMN = "id"
        const val QUESTION_ID_COLUMN = "questionId"
        const val TEXT_COLUMN = "answerText"
        const val IS_ANSWER_CORRECT_COLUMN = "isAnswerCorrect"
        const val IS_ANSWER_SELECTED_COLUMN = "isAnswerSelected"
        const val POSITION_COLUMN = "answerPosition"
    }

}