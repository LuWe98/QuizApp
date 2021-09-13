package com.example.quizapp.model.room.entities

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = Constants.QUESTIONARY_TABLE_NAME
)
@Parcelize
data class Questionnaire(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val author: String,
    val faculty : String,
    val courseOfStudies : String,
    val subject : String) : EntityMarker() {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Questionnaire>(){
            override fun areItemsTheSame(oldItem: Questionnaire, newItem: Questionnaire) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Questionnaire, newItem: Questionnaire) = oldItem == newItem
        }
    }
}