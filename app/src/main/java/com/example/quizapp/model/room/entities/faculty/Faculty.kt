package com.example.quizapp.model.room.entities.faculty

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.model.room.entities.EntityMarker
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(
    tableName = Constants.FACULTY_TABLE_NAME,
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["abbreviation"], unique = true)
    ]
)
@Parcelize
data class Faculty(
    @PrimaryKey var id: String = ObjectId().toString(),
    var abbreviation: String,
    var name: String
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Faculty> { old, new ->  old.id == new.id}
    }

}