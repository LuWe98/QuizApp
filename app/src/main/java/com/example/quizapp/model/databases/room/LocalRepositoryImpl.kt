package com.example.quizapp.model.databases.room

import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.quizapp.extensions.asRawQueryPlaceHolderString
import com.example.quizapp.extensions.div
import com.example.quizapp.model.databases.room.dao.*
import com.example.quizapp.model.databases.room.entities.*
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.datastore.datawrappers.LocalQuestionnaireOrderBy
import com.example.quizapp.model.ktor.status.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class LocalRepositoryImpl @Inject constructor(
    private val localDatabase: LocalDatabase,
    private val questionnaireDao: QuestionnaireDao,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val facultyDao: FacultyDao,
    private val courseOfStudiesDao: CourseOfStudiesDao,
    private val questionnaireCourseOfStudiesRelationDao: QuestionnaireCourseOfStudiesRelationDao,
    private val facultyCourseOfStudiesRelationDao: FacultyCourseOfStudiesRelationDao,
    private val locallyDeletedQuestionnaireDao: LocallyDeletedQuestionnaireDao,
    private val locallyFilledQuestionnaireToUploadDao: LocallyFilledQuestionnaireToUploadDao
) : LocalRepository {

    /**
     * This method returns the DAO object for the given Entity Class
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : EntityMarker> getBaseDaoWith(entity: T): BaseDao<T> = when (entity::class as KClass<T>) {
        Answer::class -> answerDao
        Question::class -> questionDao
        Questionnaire::class -> questionnaireDao
        Faculty::class -> facultyDao
        CourseOfStudies::class -> courseOfStudiesDao
        QuestionnaireCourseOfStudiesRelation::class -> questionnaireCourseOfStudiesRelationDao
        FacultyCourseOfStudiesRelation::class -> facultyCourseOfStudiesRelationDao
        LocallyDeletedQuestionnaire::class -> locallyDeletedQuestionnaireDao
        LocallyFilledQuestionnaireToUpload::class -> locallyFilledQuestionnaireToUploadDao
        else -> throw IllegalArgumentException("Entity DAO for entity class '${entity::class.simpleName}' not found! Did you add it to the 'getBaseDaoWith' Method?")
    } as BaseDao<T>

    override suspend fun <T : EntityMarker> insert(entity: T) = getBaseDaoWith(entity).insert(entity)

    override suspend fun <T : EntityMarker> insert(entities: Collection<T>) = entities.firstOrNull()?.let(::getBaseDaoWith)?.insert(entities)

    override suspend fun <T : EntityMarker> update(entity: T) = getBaseDaoWith(entity).update(entity)

    override suspend fun <T : EntityMarker> update(entities: Collection<T>) = entities.firstOrNull()?.let(::getBaseDaoWith)?.update(entities)

    override suspend fun <T : EntityMarker> delete(entity: T) = getBaseDaoWith(entity).delete(entity)

    override suspend fun <T : EntityMarker> delete(entities: Collection<T>) = entities.firstOrNull()?.let(::getBaseDaoWith)?.delete(entities)


    override suspend fun insertCompleteQuestionnaire(completeQuestionnaire: CompleteQuestionnaire) {
        localDatabase.withTransaction {
            deleteQuestionnaireWith(completeQuestionnaire.questionnaire.id)
            insert(completeQuestionnaire.questionnaire)
            insert(completeQuestionnaire.allQuestions)
            insert(completeQuestionnaire.allAnswers)
            insert(completeQuestionnaire.asQuestionnaireCourseOfStudiesRelations)
        }
    }

    override suspend fun insertCompleteQuestionnaires(completeQuestionnaires: List<CompleteQuestionnaire>) {
        localDatabase.withTransaction {
            deleteQuestionnairesWith(completeQuestionnaires.map(CompleteQuestionnaire::questionnaire / Questionnaire::id))
            insert(completeQuestionnaires.map(CompleteQuestionnaire::questionnaire))
            insert(completeQuestionnaires.flatMap(CompleteQuestionnaire::allQuestions))
            insert(completeQuestionnaires.flatMap(CompleteQuestionnaire::allAnswers))
            insert(completeQuestionnaires.flatMap(CompleteQuestionnaire::asQuestionnaireCourseOfStudiesRelations))
        }
    }

    override suspend fun deleteAllUserData() {
        localDatabase.withTransaction {
            questionnaireDao.deleteAll()
            locallyFilledQuestionnaireToUploadDao.deleteAll()
            locallyDeletedQuestionnaireDao.deleteAll()
        }
    }

    override suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>) = questionnaireDao.deleteQuestionnairesWith(questionnaireIds)

    override suspend fun deleteQuestionnaireWith(questionnaireId: String) = questionnaireDao.deleteQuestionnaireWith(questionnaireId)

    override suspend fun deleteLocallyDeletedQuestionnaireWith(questionnaireId: String) =
        locallyDeletedQuestionnaireDao.deleteLocallyDeletedQuestionnaireWith(questionnaireId)

    override suspend fun deleteFacultiesWith(facultyIds: List<String>) = facultyDao.deleteFacultiesWith(facultyIds)

    override suspend fun deleteWhereAbbreviation(abb: String) = courseOfStudiesDao.deleteWhereAbbreviation(abb)

    override suspend fun deleteCoursesOfStudiesWith(courseOfStudiesIds: List<String>) = courseOfStudiesDao.deleteCoursesOfStudiesWith(courseOfStudiesIds)

    override suspend fun deleteFacultyCourseOfStudiesRelationsWith(courseOfStudiesId: String) =
        facultyCourseOfStudiesRelationDao.deleteFacultyCourseOfStudiesRelationsWith(courseOfStudiesId)

    override suspend fun setStatusToSynced(questionnaireIdsToSync: List<String>) {
        questionnaireDao.setStatusToSynced(questionnaireIdsToSync)
    }

    override suspend fun unsyncAllSyncingQuestionnaires() {
        findAllSyncingQuestionnaires().let { questionnaires ->
            if (questionnaires.isEmpty()) return@let
            questionnaireDao.update(questionnaires.map { it.copy(syncStatus = SyncStatus.UNSYNCED) })
        }
    }

    override fun getAllLocalAuthorsFlow() = questionnaireDao.getAllLocalAuthorsFlow()

    override fun getAllFacultiesFlow() = facultyDao.getAllFacultiesFlow()

    override fun getAllCoursesOfStudiesFlow() = courseOfStudiesDao.getAllCourseOfStudiesFlow()

    override fun getAllCompleteQuestionnairesFlow() = questionnaireDao.getAllCompleteQuestionnairesFlow()

    override suspend fun findAllUnsyncedQuestionnaireIds() = questionnaireDao.findAllNonSyncedQuestionnaireIds()

    override suspend fun findAllSyncedQuestionnaires() = questionnaireDao.findAllSyncedQuestionnaires()

    override suspend fun findAllSyncingQuestionnaires() = questionnaireDao.findAllSyncingQuestionnaires()

    override suspend fun getAllQuestionnaireIds(): List<String> = questionnaireDao.getAllQuestionnaireIds()

    override suspend fun getLocallyDeletedQuestionnaireIds() = locallyDeletedQuestionnaireDao.getLocallyDeletedQuestionnaireIds()

    override suspend fun getCourseOfStudiesIdsWithTimestamp() = courseOfStudiesDao.getCourseOfStudiesIdsWithTimestamp()

    override suspend fun getFacultyIdsWithTimestamp() = facultyDao.getFacultyIdsWithTimestamp()

    override suspend fun getLocallyAnsweredCompleteQuestionnaires(): List<CompleteQuestionnaire> {
        val locallyAnsweredQuestionnaireIds = locallyFilledQuestionnaireToUploadDao.getAllLocallyFilledQuestionnairesToUploadIds()

        locallyAnsweredQuestionnaireIds.map(LocallyFilledQuestionnaireToUpload::questionnaireId).let { locallyAnswered ->
            if (locallyAnswered.isEmpty()) return emptyList()

            val foundCompleteQuestionnaires = findCompleteQuestionnairesWith(locallyAnswered)

            (locallyAnswered - foundCompleteQuestionnaires.map(CompleteQuestionnaire::questionnaire / Questionnaire::id).toSet()).let { deletedQuestionnaireIds ->
                if (deletedQuestionnaireIds.isNotEmpty()) {
                    locallyFilledQuestionnaireToUploadDao.deleteLocallyFilledQuestionnaireToUploadWith(deletedQuestionnaireIds)
                }
            }

            return foundCompleteQuestionnaires
        }
    }


    override fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireAsFlowWith(questionnaireId)

    override suspend fun findCompleteQuestionnaireWith(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireWith(questionnaireId)

    override suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>) = questionnaireDao.findCompleteQuestionnairesWith(questionnaireIds)

    override suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>, userId: String) =
        questionnaireDao.findCompleteQuestionnairesWith(questionnaireIds, userId)

    override suspend fun findQuestionnaireWith(questionnaireId: String) = questionnaireDao.findQuestionnaireWith(questionnaireId)

    override suspend fun findQuestionnairesWith(questionnaireIds: List<String>) = questionnaireDao.findQuestionnairesWith(questionnaireIds)

    override suspend fun isLocallyFilledQuestionnaireToUploadPresent(questionnaireId: String) =
        locallyFilledQuestionnaireToUploadDao.isLocallyFilledQuestionnaireToUploadPresent(questionnaireId) == 1

    override suspend fun getAuthorInfosWithName(searchQuery: String) = questionnaireDao.getAuthorInfosWithName(searchQuery)

    override suspend fun getAuthorInfosWithIds(authorIds: Set<String>) = questionnaireDao.getAuthorInfosWithIds(authorIds)

    override suspend fun getFacultiesWithIds(facultyIds: List<String>) = facultyDao.getFacultiesWithIds(facultyIds)

    override fun findFacultiesWithNameFlow(nameToSearch: String) = facultyDao.findFacultiesWithNameFlow(nameToSearch)

    override suspend fun getCoursesOfStudiesWithIds(courseOfStudiesIds: Collection<String>) = courseOfStudiesDao.getCoursesOfStudiesWithIds(courseOfStudiesIds)

    override suspend fun getCoursesOfStudiesNameWithIds(courseOfStudiesIds: List<String>) = courseOfStudiesDao.getCoursesOfStudiesNameWithIds(courseOfStudiesIds)

    override suspend fun getCourseOfStudiesWithFaculties(courseOfStudiesId: String) = courseOfStudiesDao.getCourseOfStudiesWithFaculties(courseOfStudiesId)

    override fun getCoursesOfStudiesNotAssociatedWithFacultyFlow(searchQuery: String) = courseOfStudiesDao.getCoursesOfStudiesNotAssociatedWithFacultyFlow(searchQuery)

    override fun getCoursesOfStudiesAssociatedWithFacultyFlow(facultyId: String, searchQuery: String) =
        courseOfStudiesDao.getCoursesOfStudiesAssociatedWithFacultyFlow(facultyId, searchQuery)


    override fun getFilteredCompleteQuestionnaireFlow(
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
                    "WHERE q.title LIKE ${LocalDatabase.PLACEHOLDER}"
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
                LocalQuestionnaireOrderBy.TITLE, LocalQuestionnaireOrderBy.PROGRESS -> "q.title $order"
                LocalQuestionnaireOrderBy.AUTHOR_NAME -> "q.userName $order, q.title ASC"
                LocalQuestionnaireOrderBy.QUESTION_COUNT -> "questionCount $order, q.title ASC"
                LocalQuestionnaireOrderBy.LAST_UPDATED -> "q.lastModifiedTimestamp $order, q.title ASC"
            }
        )

        queryBuilder.append(";")

        return questionnaireDao.rawQueryGetFilteredCompleteQuestionnaireFlow(SimpleSQLiteQuery(queryBuilder.toString(), args.toTypedArray())).let { questionnairesFlow ->
            questionnairesFlow.map { questionnaires ->
                val list = if (orderBy == LocalQuestionnaireOrderBy.PROGRESS) {
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