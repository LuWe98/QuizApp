package com.example.quizapp.model.room.dao

import androidx.room.Dao
import com.example.quizapp.model.room.entities.faculty.CourseOfStudies
import com.example.quizapp.utils.Constants

@Dao
abstract class CourseOfStudiesDao : BaseDao<CourseOfStudies>(Constants.COURSE_OF_STUDIES_TABLE_NAME) {


}