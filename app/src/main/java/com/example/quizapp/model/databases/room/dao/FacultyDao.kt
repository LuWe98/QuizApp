package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.junctions.FacultyWithCoursesOfStudies
import com.example.quizapp.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FacultyDao : BaseDao<Faculty>(Constants.FACULTY_TABLE_NAME) {

    @Query("SELECT facultyId, lastModifiedTimestamp FROM facultyTable")
    abstract suspend fun getFacultyIdsWithTimestamp() : List<FacultyIdWithTimeStamp>

    @get:Query("SELECT * FROM facultyTable")
    abstract val allFacultiesFlow : Flow<List<Faculty>>

    @Transaction
    @Query("SELECT * FROM facultyTable WHERE facultyId = :facultyId")
    abstract fun getCoursesOfStudiesForFacultyAlt(facultyId: String) : FacultyWithCoursesOfStudies

//    @Query("SELECT cos.* FROM facultyTable AS f JOIN facultyCourseOfStudiesRelationTable as relation ON f.facultyId = relation.facultyId JOIN courseOfStudiesTable as cos ON relation.courseOfStudiesId = cos.courseOfStudiesId")
//    abstract fun getCoursesOfStudiesForFaculty(facultyId: String) : Flow<List<CourseOfStudies>>

}