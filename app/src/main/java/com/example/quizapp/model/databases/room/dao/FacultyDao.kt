package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.databases.room.junctions.FacultyWithCoursesOfStudies
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FacultyDao : BaseDao<Faculty>(Faculty.TABLE_NAME) {

    @Query("SELECT facultyId, lastModifiedTimestamp FROM facultyTable")
    abstract suspend fun getFacultyIdsWithTimestamp(): List<FacultyIdWithTimeStamp>

    @Query("SELECT * FROM facultyTable")
    abstract fun getAllFacultiesFlow(): Flow<List<Faculty>>

    @Query("SELECT * FROM facultyTable WHERE name LIKE '%' || :nameToSearch || '%'")
    abstract fun findFacultiesWithNameFlow(nameToSearch: String): Flow<List<Faculty>>

    @Transaction
    @Query("SELECT * FROM facultyTable WHERE facultyId = :facultyId")
    abstract suspend fun getFacultyWithCourseOfStudies(facultyId: String): FacultyWithCoursesOfStudies?

    @Transaction
    @Query("SELECT * FROM facultyTable WHERE facultyId = :facultyId")
    abstract fun getFacultyWithCourseOfStudiesFlow(facultyId: String): Flow<FacultyWithCoursesOfStudies>

    @Query("SELECT * FROM facultyTable WHERE facultyId = :facultyId")
    abstract suspend fun getFacultyWithId(facultyId: String): Faculty

    @Query("SELECT * FROM facultyTable WHERE facultyId IN(:facultyIds)")
    abstract suspend fun getFacultiesWithIds(facultyIds: List<String>): List<Faculty>

    @Query("DELETE FROM facultyTable WHERE facultyId IN(:facultyIds)")
    abstract suspend fun deleteFacultiesWith(facultyIds: List<String>)

}