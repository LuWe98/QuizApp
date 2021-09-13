package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Answer
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AnswerDao : BaseDao<Answer> {

    @Query("SELECT * FROM answerTable")
    abstract suspend fun getAllAnswers() : List<Answer>

    @get:Query("SELECT * FROM answerTable")
    abstract val allAnswersFlow : Flow<List<Answer>>

    @Query("SELECT * FROM answerTable WHERE questionId =:questionId")
    abstract fun getAnswersOfQuestion(questionId : Long) : List<Answer>

    @Query("SELECT * FROM answerTable WHERE questionId =:questionId")
    abstract fun getAnswersOfQuestionFlow(questionId : Long) : Flow<List<Answer>>
}