package com.example.quizapp.model.databases.room.entities.relations

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = Constants.FACULTY_COURSE_OF_STUDIES_RELATION_TABLE_NAME,
    primaryKeys = ["facultyId", "courseOfStudiesId"],
    foreignKeys = [
        ForeignKey(
            entity = Faculty::class,
            parentColumns = ["facultyId"],
            childColumns = ["facultyId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CourseOfStudies::class,
            parentColumns = ["courseOfStudiesId"],
            childColumns = ["courseOfStudiesId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["facultyId"]),
        Index(value = ["courseOfStudiesId"])
    ]
)
@Parcelize
data class FacultyCourseOfStudiesRelation(
    val facultyId: String,
    val courseOfStudiesId: String
) : EntityMarker