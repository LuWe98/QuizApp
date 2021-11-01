package com.example.quizapp.model.databases.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import kotlinx.parcelize.Parcelize

@Parcelize
data class FacultyWithCoursesOfStudiesJunction(
    @Embedded val faculty: Faculty,
    @Relation(entity = CourseOfStudies::class, entityColumn = "facultyId", parentColumn = "id") val coursesOfStudies: List<CourseOfStudies>
): Parcelable