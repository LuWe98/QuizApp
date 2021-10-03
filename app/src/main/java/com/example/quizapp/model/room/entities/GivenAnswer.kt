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
    tableName = Constants.GIVEN_ANSWER_TABLE_NAME,
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
    @PrimaryKey var answerId: String = ObjectId().toString(),
    var isAnswerSelected: Boolean = false
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<GivenAnswer> { o, o2 -> o.answerId == o2.answerId }
    }
}