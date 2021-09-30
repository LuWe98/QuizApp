package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.GivenAnswer
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GivenAnswerDao : BaseDao<GivenAnswer> {

}