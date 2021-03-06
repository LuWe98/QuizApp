package com.example.quizapp.model.databases.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.extensions.generateDiffItemCallback
import com.example.quizapp.model.databases.properties.Degree
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(
    tableName = CourseOfStudies.TABLE_NAME,
    indices = [
        Index(value = [CourseOfStudies.ABBREVIATION_COLUMN], unique = true),
        Index(value = [CourseOfStudies.NAME_COLUMN]),
    ]
)
@Parcelize
data class CourseOfStudies(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val id: String = ObjectId().toHexString(),
    @ColumnInfo(name = ABBREVIATION_COLUMN)
    val abbreviation: String,
    @ColumnInfo(name = NAME_COLUMN)
    val name: String,
    @ColumnInfo(name = DEGREE_COLUMN)
    val degree: Degree,
    @ColumnInfo(name = LAST_MODIFIED_TIMESTAMP_COLUMN)
    val lastModifiedTimestamp : Long = getTimeMillis()
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(CourseOfStudies::id)

        const val TABLE_NAME = "courseOfStudiesTable"

        const val ID_COLUMN = "courseOfStudiesId"
        const val ABBREVIATION_COLUMN = "abbreviation"
        const val NAME_COLUMN = "name"
        const val DEGREE_COLUMN = "degree"
        const val LAST_MODIFIED_TIMESTAMP_COLUMN = "lastModifiedTimestamp"
    }
}