package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Answer
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AnswerDao : BaseDao<Answer> {

    @Query("SELECT * FROM answerTable WHERE questionId =:questionId")
    abstract fun findAnswersWithQuestionId(questionId : String) : List<Answer>

    @Query("SELECT * FROM answerTable WHERE questionId =:questionId")
    abstract fun findAnswersByIdFlow(questionId : String) : Flow<List<Answer>>

}