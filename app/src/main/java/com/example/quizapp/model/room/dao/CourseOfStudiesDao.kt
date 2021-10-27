package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.CourseOfStudies
import com.example.quizapp.utils.Constants

@Dao
abstract class CourseOfStudiesDao : BaseDao<CourseOfStudies>(Constants.COURSE_OF_STUDIES_TABLE_NAME) {


}