package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuestionDao : BaseDao<Question> {

    @Query("SELECT * FROM questionTable")
    abstract suspend fun getAllQuestions() : List<Question>

    @get:Query("SELECT * FROM questionTable")
    abstract val allQuestionsFlow : Flow<List<Question>>

    @Query("SELECT * FROM questionTable WHERE questionnaireId =:questionnaireId ORDER BY questionPosition")
    abstract fun getQuestionsOfQuestionnaire(questionnaireId : String) : List<Question>

    @Query("SELECT * FROM questionTable WHERE questionnaireId =:questionnaireId ORDER BY questionPosition")
    abstract fun getQuestionsOfQuestionnaireFlow(questionnaireId : String) : Flow<List<Question>>

    @Query("DELETE FROM questionTable WHERE questionnaireId = :questionnaireId")
    abstract fun deleteQuestionsWith(questionnaireId: String)
}