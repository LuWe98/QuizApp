package com.example.quizapp.model.room

import androidx.room.Transaction
import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.mongodb.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.room.dao.*
import com.example.quizapp.model.room.dao.sync.*
import com.example.quizapp.model.room.entities.*
import com.example.quizapp.model.room.entities.sync.*
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val locallyAnsweredQuestionnaireDao: LocallyAnsweredQuestionnaireDao,
    private val locallyDeletedUserDao: LocallyDeletedUserDao
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
        is LocallyDeletedUser -> locallyDeletedUserDao
        else -> throw IllegalArgumentException("Entity DAO could not be found! Did you add it to the 'getBaseDaoWith' - Method?")
    } as BaseDao<T>)


    suspend fun deleteAllData() {
        applicationScope.apply {
            launch(IO) { questionnaireDao.deleteAllQuestionnaires() }
            launch(IO) { locallyAnsweredQuestionnaireDao.deleteAllLocallyAnsweredQuestionnaires() }
            launch(IO) { locallyDeletedFilledQuestionnaireDao.deleteAllLocallyDeletedFilledQuestionnaires() }
            launch(IO) { locallyDeletedQuestionnaireDao.deleteAllLocallyDeletedQuestionnaires() }
            launch(IO) { locallyDownloadedQuestionnaireDao.deleteAllLocallyDownloadedQuestionnaires() }
            launch(IO) { locallyDeletedUserDao.deleteAllLocallyDeletedUsers() }
            launch(IO) { facultyDao.deleteAllFaculties() }
            launch(IO) { subjectDao.deleteAllSubjects() }
            launch(IO) { courseOfStudiesDao.deleteAllCourseOfStudies() }
            launch(IO) { localDatabase.clearAllTables() }
        }
    }


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

    suspend fun unsyncAllSyncingQuestionnaires() {
        findAllSyncingQuestionnaires().let { questionnaires ->
            if(questionnaires.isEmpty()) return@let
            questionnaireDao.update(questionnaires.onEach { it.syncStatus = SyncStatus.UNSYNCED })
        }
    }

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





    //LOCALLY DELETED QUESTIONNAIRE
    suspend fun getLocallyDeletedQuestionnaireIds() = locallyDeletedQuestionnaireDao.getLocallyDeletedQuestionnaireIds()



    //LOCALLY DELETED FILLED QUESTIONNAIRE
    suspend fun getLocallyDeletedFilledQuestionnaireIds() = locallyDeletedFilledQuestionnaireDao.getLocallyDeletedFilledQuestionnaireIds()

    suspend fun getAllLocallyDeletedFilledQuestionnaires() : List<MongoFilledQuestionnaire> {
        getLocallyDeletedFilledQuestionnaireIds().map { it.questionnaireId }.let { locallyDeleted ->
            return if(locallyDeleted.isEmpty()) emptyList()
            else {
                val foundCompleteQuestionnaires = findCompleteQuestionnairesWith(locallyDeleted)
                val deletedQuestionnaireIds = locallyDeleted - foundCompleteQuestionnaires.map { it.questionnaire.id }
                if(deletedQuestionnaireIds.isNotEmpty()){
                    locallyDeletedFilledQuestionnaireDao.deleteLocallyDeletedFilledQuestionnairesWith(deletedQuestionnaireIds)
                }
                foundCompleteQuestionnaires.map { DataMapper.mapSqlEntitiesToEmptyFilledMongoEntity(it) }
            }
        }
    }


    //LOCALLY ANSWERED QUESTIONNAIRES
    suspend fun getLocallyAnsweredQuestionnaireIds() = locallyAnsweredQuestionnaireDao.getLocallyAnsweredQuestionnaireIds()

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

    suspend fun getLocallyAnsweredQuestionnaire(questionnaireId: String) = locallyAnsweredQuestionnaireDao.getLocallyAnsweredQuestionnaire(questionnaireId)


    //LOCALLY DOWNLOADED QUESTIONNAIRE
    suspend fun getAllLocallyDownloadedQuestionnaireIds() = locallyDownloadedQuestionnaireDao.getAllDownloadedQuestionnaireIds()


    //LOCALLY DELETED USERS
    suspend fun getAllLocallyDeletedUserIds() = locallyDeletedUserDao.getAllLocallyDeletedUserIds()

}