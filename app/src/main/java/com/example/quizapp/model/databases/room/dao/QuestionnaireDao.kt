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

    //TODO -> Performance anschauen für das Progress order by
    fun getFilteredCompleteQuestionnaireFlow(
        searchQuery: String,
        orderBy: LocalQuestionnaireOrderBy,
        ascending: Boolean,
        authorIds: Set<String>,
        cosIds: Set<String>,
        facultyIds: Set<String>,
        hideCompleted: Boolean
    ): Flow<List<CompleteQuestionnaire>> {
        val args = mutableListOf<Any>("%$searchQuery%")

        val queryBuilder = StringBuilder(
            "SELECT DISTINCT q.*, COUNT(*) as questionCount " +
                    "FROM questionnaireTable as q " +
                    "LEFT JOIN questionnaireCourseOfStudiesRelationTable as qc " +
                    "ON(q.questionnaireId = qc.questionnaireId) " +
                    "LEFT JOIN courseOfStudiesTable as c " +
                    "ON(qc.courseOfStudiesId = c.courseOfStudiesId) " +
                    "LEFT JOIN facultyCourseOfStudiesRelationTable as fc " +
                    "ON(fc.courseOfStudiesId = c.courseOfStudiesId) " +
                    "LEFT JOIN facultyTable as f " +
                    "ON(fc.facultyId = f.facultyId) " +
                    "LEFT JOIN questionTable as qt " +
                    "ON(q.questionnaireId = qt.questionnaireId) " +
                    "WHERE q.title LIKE $PLACEHOLDER"
        )

        if (authorIds.isNotEmpty()) {
            queryBuilder.append(" AND q.userId IN(${authorIds.asRawQueryPlaceHolderString()})")
            args.addAll(authorIds)
        }

        if (cosIds.isNotEmpty()) {
            queryBuilder.append(" AND c.courseOfStudiesId IN(${cosIds.asRawQueryPlaceHolderString()})")
            args.addAll(cosIds)
        }

        if (facultyIds.isNotEmpty()) {
            queryBuilder.append(" AND f.facultyId IN(${facultyIds.asRawQueryPlaceHolderString()})")
            args.addAll(facultyIds)
        }

        queryBuilder.append(" GROUP BY q.questionnaireId")

        val order = if (ascending) " ASC" else " DESC"

        queryBuilder.append(" ORDER BY ").append(
            when (orderBy) {
                TITLE, PROGRESS -> "q.title $order"
                AUTHOR_NAME -> "q.userName $order, q.title ASC"
                QUESTION_COUNT -> "questionCount $order, q.title ASC"
                LAST_UPDATED -> "q.lastModifiedTimestamp $order, q.title ASC"
            }
        )

        queryBuilder.append(";")

        return rawQueryGetFilteredCompleteQuestionnaireFlow(SimpleSQLiteQuery(queryBuilder.toString(), args.toTypedArray())).let { questionnairesFlow ->
            questionnairesFlow.map { questionnaires ->
                val list = if(orderBy == PROGRESS) {
                    if (ascending) {
                        questionnaires.sortedBy(CompleteQuestionnaire::answeredQuestionsPercentage)
                    } else {
                        questionnaires.sortedByDescending(CompleteQuestionnaire::answeredQuestionsPercentage)
                    }
                } else {
                    questionnaires
                }
                return@map (if (hideCompleted) list.filter(CompleteQuestionnaire::isAnyQuestionNotCorrectlyAnswered) else list)
            }
        }
    }
}