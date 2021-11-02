package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.junctions.FacultyWithCoursesOfStudies
import com.example.quizapp.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseOfStudiesDao : BaseDao<CourseOfStudies>(Constants.COURSE_OF_STUDIES_TABLE_NAME) {

    @Query("SELECT courseOfStudiesId, lastModifiedTimestamp FROM courseOfStudiesTable")
    abstract suspend fun getCourseOfStudiesIdsWithTimestamp() : List<CourseOfStudiesIdWithTimeStamp>

    @get:Query("SELECT * FROM courseOfStudiesTable")
    abstract val allCoursesOfStudiesFlow : Flow<List<CourseOfStudies>>

}