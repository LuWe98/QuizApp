package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.ktor.status.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuestionnaireDao : BaseDao<Questionnaire>(Questionnaire.TABLE_NAME) {

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE title LIKE '%' || :searchQuery || '%' ORDER BY title")
    abstract fun getFilteredCompleteQuestionnaireFlow(searchQuery: String) : Flow<List<CompleteQuestionnaire>>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE userId != :userId ORDER BY title")
    abstract fun findAllCompleteQuestionnairesNotForUserFlow(userId: String) : Flow<List<CompleteQuestionnaire>>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE userId = :userId ORDER BY title")
    abstract fun findAllCompleteQuestionnairesForUserFlow(userId : String) : Flow<List<CompleteQuestionnaire>>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun findCompleteQuestionnaireWith(questionnaireId: String) : CompleteQuestionnaire?

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>) : List<CompleteQuestionnaire>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE userId =:userId AND questionnaireId IN(:questionnaireIds)")
    abstract suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>, userId: String) : List<CompleteQuestionnaire>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId = :questionnaireId")
    abstract fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String) : Flow<CompleteQuestionnaire>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllSyncedQuestionnaires(syncStatus: SyncStatus = SyncStatus.SYNCED): List<CompleteQuestionnaire>

    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun findQuestionnaireWith(questionnaireId: String) : Questionnaire?

    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun findQuestionnairesWith(questionnaireIds: List<String>) : List<Questionnaire>

    @Query("SELECT questionnaireId FROM questionnaireTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllNonSyncedQuestionnaireIds(syncStatus: SyncStatus = SyncStatus.UNSYNCED): List<String>

    @Query("SELECT * FROM questionnaireTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllSyncingQuestionnaires(syncStatus: SyncStatus = SyncStatus.SYNCING): List<Questionnaire>

    @Query("DELETE FROM questionnaireTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteQuestionnaireWith(questionnaireId: String)

    @Query("DELETE FROM questionnaireTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>)

    @Query("SELECT questionnaireId FROM questionnaireTable")
    abstract suspend fun getAllQuestionnaireIds() : List<String>

    @Query("UPDATE questionnaireTable SET syncStatus = :syncStatus WHERE questionnaireId IN (:questionnaireIdsToSync)")
    abstract suspend fun setStatusToSynced(questionnaireIdsToSync: List<String>, syncStatus: SyncStatus = SyncStatus.SYNCED)

}