package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuestionDao : BaseDao<Question>(Question.TABLE_NAME) {

    @Query("SELECT * FROM questionTable WHERE questionnaireId =:questionnaireId ORDER BY questionPosition")
    abstract fun findQuestionsWith(questionnaireId : String) : List<Question>

    @Query("SELECT * FROM questionTable WHERE questionnaireId =:questionnaireId ORDER BY questionPosition")
    abstract fun findQuestionsAsFlowWith(questionnaireId : String) : Flow<List<Question>>

}