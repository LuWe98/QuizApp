package com.example.quizapp.model.databases.room.entities.relations

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = FacultyCourseOfStudiesRelation.TABLE_NAME,
    primaryKeys = [
        FacultyCourseOfStudiesRelation.FACULTY_ID_COLUMN,
        FacultyCourseOfStudiesRelation.COURSE_OF_STUDIES_ID_COLUMN
   ],
    foreignKeys = [
        ForeignKey(
            entity = Faculty::class,
            parentColumns = [Faculty.ID_COLUMN],
            childColumns = [FacultyCourseOfStudiesRelation.FACULTY_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CourseOfStudies::class,
            parentColumns = [CourseOfStudies.ID_COLUMN],
            childColumns = [FacultyCourseOfStudiesRelation.COURSE_OF_STUDIES_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [FacultyCourseOfStudiesRelation.FACULTY_ID_COLUMN]),
        Index(value = [FacultyCourseOfStudiesRelation.COURSE_OF_STUDIES_ID_COLUMN])
    ]
)
@Parcelize
data class FacultyCourseOfStudiesRelation(
    @ColumnInfo(name = FACULTY_ID_COLUMN)
    val facultyId: String,
    @ColumnInfo(name = COURSE_OF_STUDIES_ID_COLUMN)
    val courseOfStudiesId: String
) : EntityMarker {

    companion object {
        const val TABLE_NAME = "facultyCourseOfStudiesRelationTable"

        const val FACULTY_ID_COLUMN = "facultyId"
        const val COURSE_OF_STUDIES_ID_COLUMN = "courseOfStudiesId"
    }

}