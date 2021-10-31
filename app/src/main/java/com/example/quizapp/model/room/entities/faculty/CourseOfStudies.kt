package com.example.quizapp.model.room.entities.faculty

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.model.room.entities.EntityMarker
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(
    tableName = Constants.COURSE_OF_STUDIES_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Faculty::class,
            parentColumns = ["id"],
            childColumns = ["facultyId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["facultyId"]),
        Index(value = ["abbreviation"], unique = true)
    ]
)
@Parcelize
data class CourseOfStudies(
    @PrimaryKey var id: String = ObjectId().toString(),
    var facultyId: String,
    var abbreviation: String,
    var name: String
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<CourseOfStudies> { old, new ->  old.id == new.id}
    }

}