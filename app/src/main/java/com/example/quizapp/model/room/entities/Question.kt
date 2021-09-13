package com.example.quizapp.model.room.entities

import androidx.recyclerview.widget.DiffUtil
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
    @PrimaryKey(autoGenerate = true) val id: Long,
    val questionnaireId: Long,
    val text: String,
    val isMultipleChoice : Boolean = true,
    val position : Int
) : EntityMarker() {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Question>(){
            override fun areItemsTheSame(oldItem: Question, newItem: Question) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Question, newItem: Question) = oldItem == newItem
        }
    }
}