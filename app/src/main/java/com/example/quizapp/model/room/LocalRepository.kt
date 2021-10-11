package com.example.quizapp.model.room

import com.example.quizapp.model.room.dao.*
import com.example.quizapp.model.room.entities.*
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val applicationScope : CoroutineScope,
    private val localDatabase: LocalDatabase,
    private val questionnaireDao: QuestionnaireDao,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val roleDao: RoleDao,
    private val facultyDao: FacultyDao,
    private val courseOfStudiesDao: CourseOfStudiesDao,
    private val subjectDao: SubjectDao,
    private val locallyDownloadedQuestionnaireDao: LocallyDownloadedQuestionnaireDao,
    private val locallyDeletedQuestionnaireDao: LocallyDeletedQuestionnaireDao
) {

    suspend fun <T : EntityMarker> insert(entity: T) = getBaseDaoWith(entity).insert(entity)

    suspend fun <T : EntityMarker> insert(entity: List<T>) = getBaseDaoWith(entity)?.insert(entity)

    suspend fun <T : EntityMarker> update(entity: T) = getBaseDaoWith(entity).update(entity)

    suspend fun <T : EntityMarker> update(entity: List<T>) = getBaseDaoWith(entity)?.update(entity)

    suspend fun <T : EntityMarker> delete(entity: T) = getBaseDaoWith(entity).delete(entity)

    suspend fun <T : EntityMarker> delete(entity: List<T>) = getBaseDaoWith(entity)?.delete(entity)

    private fun <T : EntityMarker> getBaseDaoWith(entities: List<T>) = if (entities.isEmpty()) null else getBaseDaoWith(entities[0])

    @Suppress("UNCHECKED_CAST")
    private fun <T : EntityMarker> getBaseDaoWith(entity: T) = (when (entity) {
        is Answer -> answerDao
        is Question -> questionDao
        is Questionnaire -> questionnaireDao
        is Role -> roleDao
        is Faculty -> facultyDao
        is CourseOfStudies -> courseOfStudiesDao
        is Subject -> subjectDao
        is LocallyDeletedQuestionnaire -> locallyDeletedQuestionnaireDao
        is LocallyDownloadedQuestionnaire -> locallyDownloadedQuestionnaireDao
        else -> throw IllegalArgumentException("Object does not implement EntityMarker Interface!")
    } as BaseDao<T>)


    // TRANSACTIONS
    fun test() {
        localDatabase.runInTransaction {

        }
    }


    //QUESTIONNAIRE
    fun findAllQuestionnairesWithQuestionsNotForUserFlow(userId: String) = questionnaireDao.findAllQuestionnairesWithQuestionsNotForUserFlow(userId)

    fun findAllQuestionnairesWithQuestionsForUserFlow(userId: String) = questionnaireDao.findAllQuestionnairesWithQuestionsForUserFlow(userId)

    suspend fun findCompleteQuestionnaireWith(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireWith(questionnaireId)

    fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireAsFlowWith(questionnaireId)

    suspend fun findQuestionnaireWith(questionnaireId: String) = questionnaireDao.findQuestionnaireWith(questionnaireId)

    suspend fun findQuestionnairesWith(questionnaireIds: List<String>) = questionnaireDao.findQuestionnairesWith(questionnaireIds)

    suspend fun findAllNonSyncedQuestionnaireIds() = questionnaireDao.findAllNonSyncedQuestionnaireIds()

    suspend fun findAllSyncedQuestionnaires() = questionnaireDao.findAllSyncedQuestionnaires()

    suspend fun findAllSyncingQuestionnaires() = questionnaireDao.findAllSyncingQuestionnaires()

    suspend fun deleteAllQuestionnaires() = questionnaireDao.deleteAllQuestionnaires()

    suspend fun findCompleteQuestionnaireWith(questionnaireIds: List<String>) = questionnaireDao.findCompleteQuestionnaireWith(questionnaireIds)

    suspend fun deleteQuestionnaireWith(questionnaireId: String) {
        findQuestionnaireWith(questionnaireId)?.let {
            delete(it)
        }
    }

    suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>) {
        delete(findQuestionnairesWith(questionnaireIds))
    }



    //QUESTION
    fun findQuestionsAsFlowWith(questionnaireId: String) = questionDao.findQuestionsAsFlowWith(questionnaireId)


    //ANSWER
    fun findAnswersByIdFlow(questionId: String) = answerDao.findAnswersByIdFlow(questionId)

    fun findAllSelectedAnswersWithQuestionId() = answerDao.findAllSelectedAnswersWithQuestionId()


    //DOWNLOADED QUESTIONNAIRE
    suspend fun getAllLocallyDownloadedQuestionnaireIds() = locallyDownloadedQuestionnaireDao.getAllDownloadedQuestionnaireIds()


    //DELETED QUESTIONNAIRE
    suspend fun getAllDeletedQuestionnaireIds() = locallyDeletedQuestionnaireDao.getAllDeletedQuestionnaireIds()

}