package com.example.quizapp.model.room.entities

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
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
    @PrimaryKey(autoGenerate = true) override val id: Long,
    val questionId: Long,
    val text: String,
    val isAnswerCorrect: Boolean,
    var isAnswerSelected: Boolean = false,
    val position: Int = 0
) : EntityMarker(id) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Answer>() {
            override fun areItemsTheSame(oldItem: Answer, newItem: Answer) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Answer, newItem: Answer) = oldItem == newItem
        }

        fun createEmptyAnswer(position: Int) = Answer(System.currentTimeMillis() * -1, -1, "", isAnswerCorrect = false, position = position)
    }

}