package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

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
    @PrimaryKey var id: String = ObjectId().toString(),
    var questionId: String,
    var answerText: String,
    var isAnswerCorrect: Boolean,
    var isAnswerSelected: Boolean = false,
    var position: Int = 0
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Answer> { o, o2 ->  o.id == o2.id}

        fun createEmptyAnswer(position: Int) = Answer(questionId= "", answerText = "", isAnswerCorrect = false, position = position)
    }

}