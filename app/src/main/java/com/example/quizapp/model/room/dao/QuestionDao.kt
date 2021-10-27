package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuestionDao : BaseDao<Question>(Constants.QUESTION_TABLE_NAME) {

    @Query("SELECT * FROM questionTable WHERE questionnaireId =:questionnaireId ORDER BY questionPosition")
    abstract fun findQuestionsWith(questionnaireId : String) : List<Question>

    @Query("SELECT * FROM questionTable WHERE questionnaireId =:questionnaireId ORDER BY questionPosition")
    abstract fun findQuestionsAsFlowWith(questionnaireId : String) : Flow<List<Question>>

}