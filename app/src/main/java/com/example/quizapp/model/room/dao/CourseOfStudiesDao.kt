package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.CourseOfStudies

@Dao
abstract class CourseOfStudiesDao : BaseDao<CourseOfStudies> {

    @Query("DELETE FROM courseOfStudiesTableName")
    abstract suspend fun deleteAllCourseOfStudies()

}