package com.example.quizapp.model.room.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestions
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestionsAndAnswers
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuestionnaireDao : BaseDao<Questionnaire> {

    @Query("SELECT * FROM questionaryTable ORDER BY title")
    abstract suspend fun  getAllQuestionnaires() : List<Questionnaire>

    @get:Query("SELECT * FROM questionaryTable ORDER BY title")
    abstract val allQuestionnairesFlow : Flow<List<Questionnaire>>

    @Transaction
    @Query("SELECT * FROM questionaryTable ORDER BY title")
    abstract fun getAllQuestionnairesWithQuestions() : Flow<List<QuestionnaireWithQuestions>>

    @Transaction
    @Query("SELECT * FROM questionaryTable ORDER BY title")
    abstract fun getAllQuestionnairesWithQuestionsPagingSource() : DataSource.Factory<Int, QuestionnaireWithQuestions>

//    @Transaction
//    @Query("SELECT * FROM questionaryTable ORDER BY title")
//    abstract fun getAllQuestionnairesWithQuestionsPagingSource() : PagingSource<Int, QuestionnaireWithQuestions>

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE id = :questionnaireId")
    abstract suspend fun getCompleteQuestionnaireWithId(questionnaireId: Long) : QuestionnaireWithQuestionsAndAnswers

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE id = :questionnaireId")
    abstract fun getCompleteQuestionnaireWithIdLiveData(questionnaireId: Long) : LiveData<QuestionnaireWithQuestionsAndAnswers>

    @Query("SELECT * FROM questionaryTable WHERE faculty =:faculty ORDER BY title")
    abstract fun getQuestionnaireWithFaculty(faculty: String) : Flow<List<Questionnaire>>
}