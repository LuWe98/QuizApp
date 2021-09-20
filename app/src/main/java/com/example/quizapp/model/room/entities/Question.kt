package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = Constants.QUESTION_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Questionnaire::class,
            parentColumns = ["id"],
            childColumns = ["questionnaireId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["questionnaireId"])
    ]
)
@Parcelize
data class Question(
    @PrimaryKey(autoGenerate = true) override val id: Long,
    val questionnaireId: Long,
    val text: String,
    val isMultipleChoice: Boolean = true,
    val position: Int
) : EntityMarker(id) {

    companion object {
        val DIFF_CALLBACK = createBasicDiffUtil<Question>()
    }

}