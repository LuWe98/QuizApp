package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(
    tableName = Constants.FACULTY_TABLE_NAME,
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
@Parcelize
data class Faculty(
    @PrimaryKey val id: String = ObjectId().toString(),
    val name: String
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Faculty> { o, o2 ->  o.id == o2.id}
    }

}