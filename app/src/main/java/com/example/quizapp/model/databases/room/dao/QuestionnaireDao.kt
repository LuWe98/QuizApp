package com.example.quizapp.model.databases.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuestionnaireDao : BaseDao<Questionnaire>(Questionnaire.TABLE_NAME) {

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE userId != :userId ORDER BY title")
    abstract fun findAllCompleteQuestionnairesNotForUserFlow(userId: String) : Flow<List<CompleteQuestionnaire>>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE userId != :userId ORDER BY title")
    abstract fun findAllCompleteQuestionnairesNotForUserPagingSource(userId: String): PagingSource<Int, CompleteQuestionnaire>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE userId = :userId ORDER BY title")
    abstract fun findAllCompleteQuestionnairesForUserFlow(userId : String) : Flow<List<CompleteQuestionnaire>>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE userId = :userId ORDER BY title")
    abstract fun findAllCompleteQuestionnairesForUserPagingSource(userId: String): PagingSource<Int, CompleteQuestionnaire>


    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE id = :questionnaireId")
    abstract suspend fun findCompleteQuestionnaireWith(questionnaireId: String) : CompleteQuestionnaire?

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE id IN(:questionnaireIds)")
    abstract suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>) : List<CompleteQuestionnaire>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE userId =:userId AND id IN(:questionnaireIds)")
    abstract suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>, userId: String) : List<CompleteQuestionnaire>

    @Query("SELECT * FROM questionnaireTable WHERE id = :questionnaireId")
    abstract suspend fun findQuestionnaireWith(questionnaireId: String) : Questionnaire?

    @Query("SELECT * FROM questionnaireTable WHERE id IN(:questionnaireIds)")
    abstract suspend fun findQuestionnairesWith(questionnaireIds: List<String>) : List<Questionnaire>


    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE id = :questionnaireId")
    abstract fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String) : Flow<CompleteQuestionnaire>

    @Query("SELECT id FROM questionnaireTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllNonSyncedQuestionnaireIds(syncStatus: SyncStatus = SyncStatus.UNSYNCED): List<String>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllSyncedQuestionnaires(syncStatus: SyncStatus = SyncStatus.SYNCED): List<CompleteQuestionnaire>

    @Query("SELECT * FROM questionnaireTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllSyncingQuestionnaires(syncStatus: SyncStatus = SyncStatus.SYNCING): List<Questionnaire>

    @Query("DELETE FROM questionnaireTable WHERE id = :questionnaireId")
    abstract suspend fun deleteQuestionnaireWith(questionnaireId: String)

    @Query("DELETE FROM questionnaireTable WHERE id IN(:questionnaireIds)")
    abstract suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>)

    @Query("SELECT id FROM questionnaireTable")
    abstract suspend fun getAllQuestionnaireIds() : List<String>

    @Query("UPDATE questionnaireTable SET syncStatus = :syncStatus WHERE id IN (:questionnaireIdsToSync)")
    abstract suspend fun setStatusToSynced(questionnaireIdsToSync: List<String>, syncStatus: SyncStatus = SyncStatus.SYNCED)

}