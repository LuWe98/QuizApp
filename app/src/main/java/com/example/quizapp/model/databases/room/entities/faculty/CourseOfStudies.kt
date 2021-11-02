package com.example.quizapp.model.databases.room.entities.faculty

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.Degree
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffCallbackUtil
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(
    tableName = Constants.COURSE_OF_STUDIES_TABLE_NAME,
    indices = [
        Index(value = ["abbreviation"], unique = true)
    ]
)
@Parcelize
data class CourseOfStudies(
    @PrimaryKey @ColumnInfo(name = "courseOfStudiesId") var id: String = ObjectId().toString(),
    var abbreviation: String,
    var name: String,
    var degree: Degree,
    var lastModifiedTimestamp : Long = getTimeMillis()
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<CourseOfStudies> { old, new ->  old.id == new.id}
    }

}