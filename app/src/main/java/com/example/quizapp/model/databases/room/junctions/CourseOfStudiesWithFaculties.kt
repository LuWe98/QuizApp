package com.example.quizapp.model.databases.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.relations.FacultyCourseOfStudiesRelation
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourseOfStudiesWithFaculties(
    @Embedded var courseOfStudies: CourseOfStudies,
    @Relation(
        entity = Faculty::class,
        entityColumn = FacultyCourseOfStudiesRelation.FACULTY_ID_COLUMN,
        parentColumn = FacultyCourseOfStudiesRelation.COURSE_OF_STUDIES_ID_COLUMN,
        associateBy = Junction(FacultyCourseOfStudiesRelation::class)
    )
    var faculties: List<Faculty>
): Parcelable {

    val facultyCount get() = faculties.size

}