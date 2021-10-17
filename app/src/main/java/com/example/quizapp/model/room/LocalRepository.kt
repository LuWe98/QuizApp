package com.example.quizapp.model.room

import androidx.room.Transaction
import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.mongodb.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.room.dao.*
import com.example.quizapp.model.room.dao.sync.LocallyAnsweredQuestionnaireDao
import com.example.quizapp.model.room.dao.sync.LocallyDeletedFilledQuestionnaireDao
import com.example.quizapp.model.room.dao.sync.LocallyDeletedQuestionnaireDao
import com.example.quizapp.model.room.dao.sync.LocallyDownloadedQuestionnaireDao
import com.example.quizapp.model.room.entities.*
import com.example.quizapp.model.room.entities.sync.LocallyAnsweredQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDeletedFilledQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDownloadedQuestionnaire
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
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
    private val facultyDao: FacultyDao,
    private val courseOfStudiesDao: CourseOfStudiesDao,
    private val subjectDao: SubjectDao,
    private val locallyDownloadedQuestionnaireDao: LocallyDownloadedQuestionnaireDao,
    private val locallyDeletedQuestionnaireDao: LocallyDeletedQuestionnaireDao,
    private val locallyDeletedFilledQuestionnaireDao: LocallyDeletedFilledQuestionnaireDao,
    private val locallyAnsweredQuestionnaireDao: LocallyAnsweredQuestionnaireDao
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
        is Faculty -> facultyDao
        is CourseOfStudies -> courseOfStudiesDao
        is Subject -> subjectDao
        is LocallyDeletedQuestionnaire -> locallyDeletedQuestionnaireDao
        is LocallyDeletedFilledQuestionnaire -> locallyDeletedFilledQuestionnaireDao
        is LocallyAnsweredQuestionnaire -> locallyAnsweredQuestionnaireDao
        is LocallyDownloadedQuestionnaire -> locallyDownloadedQuestionnaireDao
        else -> throw IllegalArgumentException("Object does not implement EntityMarker Interface!")
    } as BaseDao<T>)



    //QUESTIONNAIRE
    @Transaction
    suspend fun insertCompleteQuestionnaire(completeCompleteQuestionnaire: CompleteQuestionnaireJunction){
        deleteQuestionnaireWith(completeCompleteQuestionnaire.questionnaire.id)
        insert(completeCompleteQuestionnaire.questionnaire)
        insert(completeCompleteQuestionnaire.allQuestions)
        insert(completeCompleteQuestionnaire.allAnswers)
    }

    @Transaction
    suspend fun insertCompleteQuestionnaires(completeCompleteQuestionnaires: List<CompleteQuestionnaireJunction>){
        deleteQuestionnairesWith(completeCompleteQuestionnaires.map { it.questionnaire.id })
        insert(completeCompleteQuestionnaires.map { it.questionnaire })
        insert(completeCompleteQuestionnaires.flatMap { it.allQuestions })
        insert(completeCompleteQuestionnaires.flatMap { it.allAnswers })
    }

    private suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>) {
        delete(findQuestionnairesWith(questionnaireIds))
    }

    fun findAllCompleteQuestionnairesNotForUserFlow(userId: String) = questionnaireDao.findAllCompleteQuestionnairesNotForUserFlow(userId)

    fun findAllCompleteQuestionnairesForUserFlow(userId: String) = questionnaireDao.findAllCompleteQuestionnairesForUserFlow(userId)

    suspend fun findCompleteQuestionnaireWith(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireWith(questionnaireId)

    suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>) = questionnaireDao.findCompleteQuestionnairesWith(questionnaireIds)

    fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireAsFlowWith(questionnaireId)

    suspend fun findQuestionnaireWith(questionnaireId: String) = questionnaireDao.findQuestionnaireWith(questionnaireId)

    suspend fun findQuestionnairesWith(questionnaireIds: List<String>) = questionnaireDao.findQuestionnairesWith(questionnaireIds)

    suspend fun findAllNonSyncedQuestionnaireIds() = questionnaireDao.findAllNonSyncedQuestionnaireIds()

    suspend fun findAllSyncedQuestionnaires() = questionnaireDao.findAllSyncedQuestionnaires()

    suspend fun findAllSyncingQuestionnaires() = questionnaireDao.findAllSyncingQuestionnaires()

    suspend fun deleteAllQuestionnaires() = questionnaireDao.deleteAllQuestionnaires()

    suspend fun deleteQuestionnaireWith(questionnaireId: String) {
        findQuestionnaireWith(questionnaireId)?.let {
            delete(it)
        }
    }




    //QUESTION
    fun findQuestionsAsFlowWith(questionnaireId: String) = questionDao.findQuestionsAsFlowWith(questionnaireId)




    //ANSWER
    fun findAnswersByIdFlow(questionId: String) = answerDao.findAnswersByIdFlow(questionId)

    fun findAllSelectedAnswersWithQuestionId() = answerDao.findAllSelectedAnswersWithQuestionId()





    //LOCALLY DOWNLOADED QUESTIONNAIRE
    suspend fun getAllLocallyDownloadedQuestionnaireIds() = locallyDownloadedQuestionnaireDao.getAllDownloadedQuestionnaireIds()


    //LOCALLY DELETED QUESTIONNAIRE
    suspend fun getLocallyDeletedQuestionnaireIds() = locallyDeletedQuestionnaireDao.getLocallyDeletedQuestionnaireIds()


    //LOCALLY DELETED FILLED QUESTIONNAIRE
    suspend fun getLocallyDeletedFilledQuestionnaireIds() = locallyDeletedFilledQuestionnaireDao.getLocallyDeletedFilledQuestionnaireIds()


    //LOCALLY ANSWERED QUESTIONNAIRES
    suspend fun getLocallyAnsweredQuestionnaireIds() = locallyAnsweredQuestionnaireDao.getLocallyDeletedQuestionnaireIds()

    suspend fun getAllLocallyAnsweredFilledQuestionnaires() : List<MongoFilledQuestionnaire> {
        getLocallyAnsweredQuestionnaireIds().map { it.questionnaireId }.let { locallyAnswered ->
            return if(locallyAnswered.isEmpty()) emptyList()
            else {
                val foundCompleteQuestionnaires = findCompleteQuestionnairesWith(locallyAnswered)
                val deletedQuestionnaireIds = locallyAnswered - foundCompleteQuestionnaires.map { it.questionnaire.id }
                if(deletedQuestionnaireIds.isNotEmpty()){
                    locallyAnsweredQuestionnaireDao.deleteLocallyAnsweredQuestionnaireWith(deletedQuestionnaireIds)
                }
                foundCompleteQuestionnaires.map { DataMapper.mapSqlEntitiesToFilledMongoEntity(it) }
            }
        }
    }

    suspend fun isAnsweredQuestionnairePresent(questionnaireId: String) = locallyAnsweredQuestionnaireDao.isAnsweredQuestionnairePresent(questionnaireId) == 1



    //LOCALLY SYNC HELPER STUFF
    //-> Wenn man lokal einen Fragebogen ausfüllt, diesen aber da
    suspend fun insertLocallyAnsweredQuestionnaire(questionnaireId: String){
        locallyAnsweredQuestionnaireDao.insert(LocallyAnsweredQuestionnaire(questionnaireId))
        locallyDeletedFilledQuestionnaireDao.deleteLocallyDeletedFilledQuestionnaireWith(questionnaireId)

    }

    //TODO -> Wenn man einen Fragebogen lokal ausgefüllt hat und dann anschließend
    suspend fun insertLocallyDeletedFilledQuestionnaire(questionnaireId: String){
        locallyDeletedFilledQuestionnaireDao.insert(LocallyDeletedFilledQuestionnaire(questionnaireId))
        locallyAnsweredQuestionnaireDao.deleteLocallyAnsweredQuestionnaireWith(questionnaireId)
    }

}