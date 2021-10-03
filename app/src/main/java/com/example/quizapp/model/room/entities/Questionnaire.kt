package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(
    tableName = Constants.QUESTIONARY_TABLE_NAME
)
@Parcelize
data class Questionnaire(
    @PrimaryKey var id: String = ObjectId().toString(),
    var title: String,
    var author: String,
    var courseOfStudies: String,
    var faculty: String,
    var subject: String
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Questionnaire> { old, new ->  old.id == new.id}
    }

}