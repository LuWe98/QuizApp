package com.example.quizapp.model.room

import com.example.quizapp.model.room.dao.BaseDao
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.EntityMarker
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(roomDatabase: LocalDatabase) {

    private val questionnaireDao = roomDatabase.getQuestionaryDao()
    private val questionDao = roomDatabase.getQuestionDao()
    private val answerDao = roomDatabase.getAnswerDao()


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




    //Questionnaire
    suspend fun getAllQuestionnaires() = questionnaireDao.getAllQuestionnaires()

    val allQuestionnairesFlow get() = questionnaireDao.allQuestionnairesFlow

    val allQuestionnairesWithQuestions get() = questionnaireDao.getAllQuestionnairesWithQuestions()

    fun getQuestionnaireWithFaculty(faculty: String) = questionnaireDao.getQuestionnaireWithFaculty(faculty)

    suspend fun getCompleteQuestionnaireWithId(questionnaireId: Long) = questionnaireDao.getCompleteQuestionnaireWithId(questionnaireId)

    fun getCompleteQuestionnaireWithIdLiveData(questionnaireId: Long) = questionnaireDao.getCompleteQuestionnaireWithIdLiveData(questionnaireId)

    val allQuestionnairesWithQuestionsPagingSource get() = questionnaireDao.getAllQuestionnairesWithQuestionsPagingSource()


    //Question
    suspend fun getAllQuestions() = questionDao.getAllQuestions()

    val getAllQuestionsFlow get() = questionDao.allQuestionsFlow

    suspend fun getQuestionsOfQuestionnaire(questionnaireId : Long) = questionDao.getQuestionsOfQuestionnaireFlow(questionnaireId)

    fun deleteQuestionsWith(questionnaireId: Long) { questionDao.deleteQuestionsWith(questionnaireId)}



    //Answer
    suspend fun allAnswers() = answerDao.getAllAnswers()

    val allAnswersFlow get() = answerDao.allAnswersFlow

    suspend fun getAnswersOfQuestion(questionId : Long) = answerDao.getAnswersOfQuestionFlow(questionId)
}