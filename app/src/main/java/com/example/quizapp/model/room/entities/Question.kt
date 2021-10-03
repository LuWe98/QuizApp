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
    @PrimaryKey var id: String = ObjectId().toString(),
    var questionnaireId: String,
    var questionText: String,
    var isMultipleChoice: Boolean = true,
    var questionPosition: Int
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Question> { old, new ->  old.id == new.id}
    }

}