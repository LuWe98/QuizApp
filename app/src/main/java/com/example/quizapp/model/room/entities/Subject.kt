package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = Constants.SUBJECT_TABLE_NAME,
    indices = [
        Index(value = ["name"])
    ]
)
@Parcelize
data class Subject(
    @PrimaryKey(autoGenerate = true) override val id: Long,
    val name: String
) : EntityMarker(id) {

    companion object {
        val DIFF_CALLBACK = createBasicDiffUtil<Subject>()
    }

}