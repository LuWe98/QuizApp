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

    @get:Query("SELECT * FROM courseOfStudiesTable")
    abstract val allCoursesOfStudiesFlow : Flow<List<CourseOfStudies>>

    @Query("DELETE FROM courseOfStudiesTable WHERE abbreviation = :abb")
    abstract suspend fun deleteWhereAbbreviation(abb: String)

    @Query("SELECT abbreviation FROM courseOfStudiesTable WHERE courseOfStudiesId =:courseOfStudiesId LIMIT 1")
    abstract suspend fun getCourseOfStudiesNameWithId(courseOfStudiesId: String): String

}