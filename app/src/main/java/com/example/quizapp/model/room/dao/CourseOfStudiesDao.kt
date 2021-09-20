package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.CourseOfStudies
import com.example.quizapp.model.room.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseOfStudiesDao : BaseDao<CourseOfStudies> {

}