package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestions
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuestionnaireDao : BaseDao<Questionnaire> {

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE userId != :userId ORDER BY title")
    abstract fun findAllCompleteQuestionnairesNotForUserFlow(userId : String) : Flow<List<CompleteQuestionnaireJunction>>

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE userId = :userId ORDER BY title")
    abstract fun findAllCompleteQuestionnairesForUserFlow(userId : String) : Flow<List<CompleteQuestionnaireJunction>>

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE id = :questionnaireId")
    abstract suspend fun findCompleteQuestionnaireWith(questionnaireId: String) : CompleteQuestionnaireJunction?

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE id IN(:questionnaireIds)")
    abstract suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>) : List<CompleteQuestionnaireJunction>

    @Query("SELECT * FROM questionaryTable WHERE id = :questionnaireId")
    abstract suspend fun findQuestionnaireWith(questionnaireId: String) : Questionnaire?

    @Query("SELECT * FROM questionaryTable WHERE id IN(:questionnaireIds)")
    abstract suspend fun findQuestionnairesWith(questionnaireIds: List<String>) : List<Questionnaire>


    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE id = :questionnaireId")
    abstract fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String) : Flow<CompleteQuestionnaireJunction>

    @Query("SELECT id FROM questionaryTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllNonSyncedQuestionnaireIds(syncStatus: SyncStatus = SyncStatus.UNSYNCED): List<String>

    @Transaction
    @Query("SELECT * FROM questionaryTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllSyncedQuestionnaires(syncStatus: SyncStatus = SyncStatus.SYNCED): List<CompleteQuestionnaireJunction>

    @Query("SELECT * FROM questionaryTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllSyncingQuestionnaires(syncStatus: SyncStatus = SyncStatus.SYNCING): List<Questionnaire>

    @Query("DELETE FROM questionaryTable")
    abstract suspend fun deleteAllQuestionnaires()


    @Query("DELETE FROM questionTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteQuestionnaireWith(questionnaireId: String)

    @Query("DELETE FROM questionTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>)

//    @Transaction
//    @Query("SELECT * FROM questionaryTable ORDER BY title")
//    abstract fun getAllQuestionnairesWithQuestionsPagingSource() : DataSource.Factory<Int, QuestionnaireWithQuestions>

//    @Transaction
//    @Query("SELECT * FROM questionaryTable ORDER BY title")
//    abstract fun getAllQuestionnairesWithQuestionsPagingSource() : PagingSource<Int, QuestionnaireWithQuestions>


    @Query("SELECT id FROM questionaryTable")
    abstract suspend fun getAllQuestionnaireIds() : List<String>
}