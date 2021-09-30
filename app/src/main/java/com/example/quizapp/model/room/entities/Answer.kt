package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = Constants.ANSWER_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["questionId"])
    ]
)
@Parcelize
data class Answer(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val questionId: Long,
    val text: String,
    val isAnswerCorrect: Boolean,
    var isAnswerSelected: Boolean = false,
    val position: Int = 0
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Answer> { o, o2 ->  o.id == o2.id}

        fun createEmptyAnswer(position: Int) = Answer(System.currentTimeMillis() * -1, -1, "", isAnswerCorrect = false, position = position)
    }

}