package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = Constants.QUESTIONARY_TABLE_NAME
)
@Parcelize
data class Questionnaire(
    @PrimaryKey(autoGenerate = true) override val id: Long,
    val title: String,
    val author: String,
    val faculty: String,
    val courseOfStudies: String,
    val subject: String
) : EntityMarker(id) {

    companion object {
        val DIFF_CALLBACK = createBasicDiffUtil<Questionnaire>()
    }

}