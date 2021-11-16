package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseOfStudiesDao : BaseDao<CourseOfStudies>(CourseOfStudies.TABLE_NAME) {

    @Query("SELECT courseOfStudiesId, lastModifiedTimestamp FROM courseOfStudiesTable")
    abstract suspend fun getCourseOfStudiesIdsWithTimestamp() : List<CourseOfStudiesIdWithTimeStamp>

    @Query("SELECT * FROM courseOfStudiesTable")
    abstract fun getAllCourseOfStudiesFlow() : Flow<List<CourseOfStudies>>

    @Query("DELETE FROM courseOfStudiesTable WHERE abbreviation = :abb")
    abstract suspend fun deleteWhereAbbreviation(abb: String)

    @Query("SELECT * FROM courseOfStudiesTable WHERE courseOfStudiesId =:courseOfStudiesId LIMIT 1")
    abstract suspend fun getCourseOfStudiesWithId(courseOfStudiesId: String) : CourseOfStudies

    @Query("SELECT abbreviation FROM courseOfStudiesTable WHERE courseOfStudiesId =:courseOfStudiesId LIMIT 1")
    abstract suspend fun getCourseOfStudiesNameWithId(courseOfStudiesId: String): String

    @Query("SELECT abbreviation FROM courseOfStudiesTable WHERE courseOfStudiesId IN(:courseOfStudiesIds)")
    abstract suspend fun getCoursesOfStudiesNameWithIds(courseOfStudiesIds: List<String>): List<String>

    @Query("SELECT * FROM courseOfStudiesTable WHERE courseOfStudiesId IN(:courseOfStudiesIds)")
    abstract fun getCoursesOfStudiesFlowWithIds(courseOfStudiesIds: List<String>): Flow<List<CourseOfStudies>>

}