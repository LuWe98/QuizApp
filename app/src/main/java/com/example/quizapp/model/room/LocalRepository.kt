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
    suspend fun getAllQuestionnaires() = questionnaireDao.getAllQuestionnaires()

    val allQuestionnairesFlow get() = questionnaireDao.allQuestionnairesFlow

    val allQuestionnairesWithQuestions get() = questionnaireDao.getAllQuestionnairesWithQuestions()

    suspend fun getCompleteQuestionnaireWithId(questionnaireId: String) = questionnaireDao.getCompleteQuestionnaireWithId(questionnaireId)

    fun getCompleteQuestionnaireWithIdLiveData(questionnaireId: String) = questionnaireDao.getCompleteQuestionnaireWithIdLiveData(questionnaireId)

    val allQuestionnairesWithQuestionsPagingSource get() = questionnaireDao.getAllQuestionnairesWithQuestionsPagingSource()

    val allQuestionnairesWithQuestionsLiveData get() = questionnaireDao.getAllQuestionnairesWithQuestionsLiveData()

    fun completeQuestionnaireStateFlow(questionnaireId: String) = questionnaireDao.getCompleteQuestionnaireWithIdFlow(questionnaireId)
        .stateIn(applicationScope, SharingStarted.WhileSubscribed(),  null)


    //QUESTION
    suspend fun getAllQuestions() = questionDao.getAllQuestions()

    val getAllQuestionsFlow get() = questionDao.allQuestionsFlow

    fun getQuestionsOfQuestionnaire(questionnaireId: String) = questionDao.getQuestionsOfQuestionnaireFlow(questionnaireId)

    fun deleteQuestionsWith(questionnaireId: String) {
        questionDao.deleteQuestionsWith(questionnaireId)
    }


    //ANSWER
    suspend fun allAnswers() = answerDao.getAllAnswers()

    val allAnswersFlow get() = answerDao.allAnswersFlow

    fun getAnswersOfQuestion(questionId: String) = answerDao.getAnswersOfQuestionFlow(questionId)
}