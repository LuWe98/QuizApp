package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = Constants.GIVEN_ANSWER_TABLE_NAME,
    primaryKeys = ["answerId", "userName"],
    foreignKeys = [
        ForeignKey(
            entity = Answer::class,
            parentColumns = ["id"],
            childColumns = ["answerId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["answerId"])
    ]
)
@Parcelize
data class GivenAnswer(
    val answerId: Long,
    val userName: String,
    val isAnswerSelected: Boolean = false
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<GivenAnswer> {
                o, o2 -> o.answerId == o2.answerId && o.userName == o2.userName
        }
    }

}