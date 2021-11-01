package com.example.quizapp.model.databases.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourseOfStudiesWithFacultyJunction(
    @Embedded val courseOfStudies: CourseOfStudies,
    @Relation(entity = CourseOfStudies::class, entityColumn = "facultyId", parentColumn = "id") val faculty: Faculty
): Parcelable