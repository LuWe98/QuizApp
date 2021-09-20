package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Faculty
import com.example.quizapp.model.room.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FacultyDao : BaseDao<Faculty> {

}