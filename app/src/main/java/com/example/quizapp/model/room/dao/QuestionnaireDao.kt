package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestions
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestionsAndAnswers
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuestionnaireDao : BaseDao<Questionnaire> {

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE userId != :userId ORDER BY title")
    abstract fun findAllQuestionnairesWithQuestionsNotForUserFlow(userId : String) : Flow<List<QuestionnaireWithQuestions>>

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE userId = :userId ORDER BY title")
    abstract fun findAllQuestionnairesWithQuestionsForUserFlow(userId : String) : Flow<List<QuestionnaireWithQuestions>>

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE id = :questionnaireId")
    abstract suspend fun findCompleteQuestionnaireWith(questionnaireId: String) : QuestionnaireWithQuestionsAndAnswers

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE id = :questionnaireId")
    abstract fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String) : Flow<QuestionnaireWithQuestionsAndAnswers>




//    @Transaction
//    @Query("SELECT * FROM questionaryTable ORDER BY title")
//    abstract fun getAllQuestionnairesWithQuestionsPagingSource() : DataSource.Factory<Int, QuestionnaireWithQuestions>

//    @Transaction
//    @Query("SELECT * FROM questionaryTable ORDER BY title")
//    abstract fun getAllQuestionnairesWithQuestionsPagingSource() : PagingSource<Int, QuestionnaireWithQuestions>
}