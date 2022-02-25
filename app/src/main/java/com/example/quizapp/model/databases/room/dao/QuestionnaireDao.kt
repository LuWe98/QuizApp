package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.quizapp.extensions.asRawQueryPlaceHolderString
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.room.LocalDatabase.Companion.PLACEHOLDER
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.Question
import com.example.quizapp.model.databases.room.entities.Questionnaire
import com.example.quizapp.model.databases.room.entities.FacultyCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.entities.QuestionnaireCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.datastore.datawrappers.LocalQuestionnaireOrderBy
import com.example.quizapp.model.datastore.datawrappers.LocalQuestionnaireOrderBy.*
import com.example.quizapp.model.ktor.status.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
abstract class QuestionnaireDao : BaseDao<Questionnaire>(Questionnaire.TABLE_NAME) {

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun findCompleteQuestionnaireWith(questionnaireId: String): CompleteQuestionnaire?

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>): List<CompleteQuestionnaire>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE userId =:userId AND questionnaireId IN(:questionnaireIds)")
    abstract suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>, userId: String): List<CompleteQuestionnaire>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId = :questionnaireId")
    abstract fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String): Flow<CompleteQuestionnaire>

    @Transaction
    @Query("SELECT * FROM questionnaireTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllSyncedQuestionnaires(syncStatus: SyncStatus = SyncStatus.SYNCED): List<CompleteQuestionnaire>

    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun findQuestionnaireWith(questionnaireId: String): Questionnaire?

    @Query("SELECT * FROM questionnaireTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun findQuestionnairesWith(questionnaireIds: List<String>): List<Questionnaire>

    @Query("SELECT questionnaireId FROM questionnaireTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllNonSyncedQuestionnaireIds(syncStatus: SyncStatus = SyncStatus.UNSYNCED): List<String>

    @Query("SELECT * FROM questionnaireTable WHERE syncStatus = :syncStatus")
    abstract suspend fun findAllSyncingQuestionnaires(syncStatus: SyncStatus = SyncStatus.SYNCING): List<Questionnaire>

    @Query("DELETE FROM questionnaireTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteQuestionnaireWith(questionnaireId: String)

    @Query("DELETE FROM questionnaireTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>)

    @Query("SELECT questionnaireId FROM questionnaireTable")
    abstract suspend fun getAllQuestionnaireIds(): List<String>

    @Query("UPDATE questionnaireTable SET syncStatus = :syncStatus WHERE questionnaireId IN (:questionnaireIdsToSync)")
    abstract suspend fun setStatusToSynced(questionnaireIdsToSync: List<String>, syncStatus: SyncStatus = SyncStatus.SYNCED)

    @Query("SELECT DISTINCT userId, userName FROM questionnaireTable WHERE userName LIKE '%' || :searchQuery || '%' ORDER BY userName")
    abstract suspend fun getAuthorInfosWithName(searchQuery: String): List<AuthorInfo>

    @Query("SELECT DISTINCT userId, userName FROM questionnaireTable WHERE userId IN(:authorIds)")
    abstract suspend fun getAuthorInfosWithIds(authorIds: Set<String>): List<AuthorInfo>

    @Query("SELECT DISTINCT userId, userName FROM questionnaireTable")
    abstract fun getAllLocalAuthorsFlow(): Flow<List<AuthorInfo>>

    @Transaction
    @Query("SELECT * FROM questionnaireTable")
    abstract fun getAllCompleteQuestionnairesFlow(): Flow<List<CompleteQuestionnaire>>

    @Transaction
    @RawQuery(
        observedEntities = [
            Questionnaire::class,
            Question::class,
            Answer::class,
            QuestionnaireCourseOfStudiesRelation::class,
            CourseOfStudies::class,
            FacultyCourseOfStudiesRelation::class,
            Faculty::class
        ]
    )
    abstract fun rawQueryGetFilteredCompleteQuestionnaireFlow(query: SimpleSQLiteQuery): Flow<List<CompleteQuestionnaire>>

}