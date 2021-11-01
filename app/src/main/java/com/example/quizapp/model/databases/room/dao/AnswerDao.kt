package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AnswerDao : BaseDao<Answer>(Constants.ANSWER_TABLE_NAME) {

    @Query("SELECT * FROM answerTable WHERE questionId =:questionId")
    abstract fun findAnswersWithQuestionId(questionId : String) : List<Answer>

    @Query("SELECT * FROM answerTable WHERE questionId =:questionId")
    abstract fun findAnswersByIdFlow(questionId : String) : Flow<List<Answer>>

    @Query("SELECT * FROM answerTable WHERE isAnswerSelected = 1")
    abstract fun findAllSelectedAnswersWithQuestionId() : List<Answer>
}