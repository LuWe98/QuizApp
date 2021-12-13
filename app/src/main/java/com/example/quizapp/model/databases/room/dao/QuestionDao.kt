package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import com.example.quizapp.model.databases.room.entities.Question

@Dao
abstract class QuestionDao : BaseDao<Question>(Question.TABLE_NAME)