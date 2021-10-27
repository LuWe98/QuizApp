package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Faculty
import com.example.quizapp.utils.Constants

@Dao
abstract class FacultyDao : BaseDao<Faculty>(Constants.FACULTY_TABLE_NAME) {


}