package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.entities.Answer
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AnswerDao : BaseDao<Answer>(Answer.TABLE_NAME)