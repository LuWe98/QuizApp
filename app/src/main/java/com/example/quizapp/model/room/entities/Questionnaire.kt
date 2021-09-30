package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = Constants.QUESTIONARY_TABLE_NAME
)
@Parcelize
data class Questionnaire(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val author: String,
    val faculty: String,
    val courseOfStudies: String,
    val subject: String
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Questionnaire> { o, o2 ->  o.id == o2.id}
    }

}