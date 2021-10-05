package com.example.quizapp.model.room

import com.example.quizapp.model.room.dao.*
import com.example.quizapp.model.room.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val applicationScope : CoroutineScope,
    private val localDatabase: LocalDatabase,
    private val questionnaireDao: QuestionnaireDao,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val givenAnswerDao: GivenAnswerDao,
    private val roleDao: RoleDao,
    private val facultyDao: FacultyDao,
    private val courseOfStudiesDao: CourseOfStudiesDao,
    private val subjectDao: SubjectDao
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
        is GivenAnswer -> givenAnswerDao
        is Question -> questionDao
        is Questionnaire -> questionnaireDao
        is Role -> roleDao
        is Faculty -> facultyDao
        is CourseOfStudies -> courseOfStudiesDao
        is Subject -> subjectDao
        else -> throw IllegalArgumentException("Object does not implement EntityMarker Interface!")
    } as BaseDao<T>)




    //QUESTIONNAIRE
    fun findAllQuestionnairesWithQuestionsNotForUserFlow(userId: String) = questionnaireDao.findAllQuestionnairesWithQuestionsNotForUserFlow(userId)

    fun findAllQuestionnairesWithQuestionsForUserFlow(userId: String) = questionnaireDao.findAllQuestionnairesWithQuestionsForUserFlow(userId)

    suspend fun findCompleteQuestionnaireWith(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireWith(questionnaireId)

    fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireAsFlowWith(questionnaireId)

    fun completeQuestionnaireStateFlow(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireAsFlowWith(questionnaireId)
        .stateIn(applicationScope, SharingStarted.WhileSubscribed(),  null)


    //QUESTION
    fun findQuestionsAsFlowWith(questionnaireId: String) = questionDao.findQuestionsAsFlowWith(questionnaireId)

    fun deleteQuestionsWith(questionnaireId: String) {
        questionDao.deleteQuestionsWith(questionnaireId)
    }


    //ANSWER
    fun getAnswersOfQuestion(questionId: String) = answerDao.findAnswersByIdFlow(questionId)
}