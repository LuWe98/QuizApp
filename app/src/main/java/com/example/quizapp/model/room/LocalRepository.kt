package com.example.quizapp.model.room

import androidx.lifecycle.LiveData
import com.example.quizapp.model.room.dao.*
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.EntityMarker
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestions
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestionsAndAnswers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val applicationScope : CoroutineScope,
    private val questionnaireDao: QuestionnaireDao,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val userDao: UserDao,
    private val userRoleDao: UserRoleDao,
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
        is Question -> questionDao
        is Questionnaire -> questionnaireDao
        else -> throw IllegalArgumentException("Object does not implement EntityMarker Interface!")
    } as BaseDao<T>)


    //QUESTIONNAIRE
    suspend fun getAllQuestionnaires() = questionnaireDao.getAllQuestionnaires()

    val allQuestionnairesFlow get() = questionnaireDao.allQuestionnairesFlow

    val allQuestionnairesWithQuestions get() = questionnaireDao.getAllQuestionnairesWithQuestions()

    suspend fun getCompleteQuestionnaireWithId(questionnaireId: Long) = questionnaireDao.getCompleteQuestionnaireWithId(questionnaireId)

    fun getCompleteQuestionnaireWithIdLiveData(questionnaireId: Long) = questionnaireDao.getCompleteQuestionnaireWithIdLiveData(questionnaireId)

    val allQuestionnairesWithQuestionsPagingSource get() = questionnaireDao.getAllQuestionnairesWithQuestionsPagingSource()

    val allQuestionnairesWithQuestionsLiveData get() = questionnaireDao.getAllQuestionnairesWithQuestionsLiveData()

    fun completeQuestionnaireStateFlow(questionnaireId: Long) = questionnaireDao.getCompleteQuestionnaireWithIdFlow(questionnaireId)
        .stateIn(applicationScope, SharingStarted.WhileSubscribed(),  null)


    //QUESTION
    suspend fun getAllQuestions() = questionDao.getAllQuestions()

    val getAllQuestionsFlow get() = questionDao.allQuestionsFlow

    fun getQuestionsOfQuestionnaire(questionnaireId: Long) = questionDao.getQuestionsOfQuestionnaireFlow(questionnaireId)

    fun deleteQuestionsWith(questionnaireId: Long) {
        questionDao.deleteQuestionsWith(questionnaireId)
    }


    //ANSWER
    suspend fun allAnswers() = answerDao.getAllAnswers()

    val allAnswersFlow get() = answerDao.allAnswersFlow

    fun getAnswersOfQuestion(questionId: Long) = answerDao.getAnswersOfQuestionFlow(questionId)
}