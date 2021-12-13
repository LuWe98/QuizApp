package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
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
    abstract suspend fun getCoursesOfStudiesWithIds(courseOfStudiesIds: Set<String>): List<CourseOfStudies>

    @Transaction
    @Query("SELECT * FROM courseOfStudiesTable WHERE courseOfStudiesId = :courseOfStudiesId")
    abstract suspend fun getCourseOfStudiesWithFaculties(courseOfStudiesId: String): CourseOfStudiesWithFaculties

    @Query("DELETE FROM courseOfStudiesTable WHERE courseOfStudiesId = :courseOfStudiesId")
    abstract suspend fun deleteCourseOfStudiesWith(courseOfStudiesId: String)

    @Query("DELETE FROM courseOfStudiesTable WHERE courseOfStudiesId IN(:courseOfStudiesIds)")
    abstract suspend fun deleteCoursesOfStudiesWith(courseOfStudiesIds: List<String>)

    @Query("SELECT c.* FROM courseOfStudiesTable as c " +
            "LEFT JOIN facultyCourseOfStudiesRelationTable as r " +
            "ON(c.courseOfStudiesId = r.courseOfStudiesId) " +
            "WHERE r.courseOfStudiesId IS NULL " +
            "AND c.name LIKE '%' || :searchQuery || '%'")
    abstract fun getCoursesOfStudiesNotAssociatedWithFacultyFlow(searchQuery: String): Flow<List<CourseOfStudies>>

    @Query("SELECT DISTINCT c.* FROM courseOfStudiesTable as c " +
            "JOIN facultyCourseOfStudiesRelationTable as r " +
            "ON(c.courseOfStudiesId = r.courseOfStudiesId) " +
            "WHERE r.facultyId = :facultyId AND c.name LIKE '%' || :searchQuery || '%'")
    abstract fun getCoursesOfStudiesAssociatedWithFacultyFlow(facultyId: String, searchQuery: String): Flow<List<CourseOfStudies>>

}