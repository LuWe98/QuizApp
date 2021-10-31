package com.example.quizapp.model.room.dao

import androidx.room.Dao
import com.example.quizapp.model.room.entities.faculty.Faculty
import com.example.quizapp.utils.Constants

@Dao
abstract class FacultyDao : BaseDao<Faculty>(Constants.FACULTY_TABLE_NAME) {


}