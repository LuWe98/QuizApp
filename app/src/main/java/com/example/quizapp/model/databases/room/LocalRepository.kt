package com.example.quizapp.model.databases.room

import androidx.room.Transaction
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.mongodb.documents.questionnairefilled.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.room.dao.*
import com.example.quizapp.model.databases.room.dao.sync.*
import com.example.quizapp.model.databases.room.entities.*
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.faculty.Subject
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.entities.relations.FacultyCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.entities.sync.*
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.FacultyWithCoursesOfStudies
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class LocalRepository @Inject constructor(
    private val localDatabase: LocalDatabase,
    private val questionnaireDao: QuestionnaireDao,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val facultyDao: FacultyDao,
    private val courseOfStudiesDao: CourseOfStudiesDao,
    private val subjectDao: SubjectDao,
    private val facultyCourseOfStudiesRelationDao : FacultyCourseOfStudiesRelationDao,
    private val locallyDeletedQuestionnaireDao: LocallyDeletedQuestionnaireDao,
    private val locallyClearedQuestionnaireDao: LocallyClearedQuestionnaireDao,
    private val locallyAnsweredQuestionnaireDao: LocallyAnsweredQuestionnaireDao,
    private val locallyDeletedUserDao: LocallyDeletedUserDao
) {

    suspend inline fun <reified T : EntityMarker> insert(entity: T) = getBaseDaoWith(T::class).insert(entity)

    suspend inline fun <reified T : EntityMarker> insert(entity: List<T>) = getBaseDaoWith(T::class).insert(entity)

    suspend inline fun <reified T : EntityMarker> update(entity: T) = getBaseDaoWith(T::class).update(entity)

    suspend inline fun <reified T : EntityMarker> update(entity: List<T>) = getBaseDaoWith(T::class).update(entity)

    suspend inline fun <reified T : EntityMarker> delete(entity: T) = getBaseDaoWith(T::class).delete(entity)

    suspend inline fun <reified T : EntityMarker> delete(entity: List<T>) = getBaseDaoWith(T::class).delete(entity)


    @Suppress("UNCHECKED_CAST")
    fun <T : EntityMarker> getBaseDaoWith(entity: KClass<T>) = (when (entity) {
        Answer::class -> answerDao
        Question::class -> questionDao
        Questionnaire::class -> questionnaireDao
        Faculty::class -> facultyDao
        CourseOfStudies::class -> courseOfStudiesDao
        Subject::class -> subjectDao
        FacultyCourseOfStudiesRelation::class -> facultyCourseOfStudiesRelationDao
        LocallyDeletedQuestionnaire::class -> locallyDeletedQuestionnaireDao
        LocallyClearedQuestionnaire::class -> locallyClearedQuestionnaireDao
        LocallyAnsweredQuestionnaire::class -> locallyAnsweredQuestionnaireDao
        LocallyDeletedUser::class -> locallyDeletedUserDao
        else -> throw IllegalArgumentException("Entity DAO could not be found! Did you add it to the 'getBaseDaoWith' - Method?")
    } as BaseDao<T>)


    suspend fun deleteAllUserData() {
        questionnaireDao.deleteAll()
        locallyAnsweredQuestionnaireDao.deleteAll()
        locallyClearedQuestionnaireDao.deleteAll()
        locallyDeletedQuestionnaireDao.deleteAll()
        locallyDeletedUserDao.deleteAll()
    }


    //QUESTIONNAIRE
    @Transaction
    suspend fun insertCompleteQuestionnaire(completeCompleteQuestionnaire: CompleteQuestionnaire) {
        deleteQuestionnaireWith(completeCompleteQuestionnaire.questionnaire.id)
        insert(completeCompleteQuestionnaire.questionnaire)
        insert(completeCompleteQuestionnaire.allQuestions)
        insert(completeCompleteQuestionnaire.allAnswers)
    }

    @Transaction
    suspend fun insertCompleteQuestionnaires(completeCompleteQuestionnaires: List<CompleteQuestionnaire>) {
        deleteQuestionnairesWith(completeCompleteQuestionnaires.map { it.questionnaire.id })
        insert(completeCompleteQuestionnaires.map { it.questionnaire })
        insert(completeCompleteQuestionnaires.flatMap { it.allQuestions })
        insert(completeCompleteQuestionnaires.flatMap { it.allAnswers })
    }

    private suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>) {
        delete(findQuestionnairesWith(questionnaireIds))
    }

    suspend fun getAllQuestionnaireIds() = questionnaireDao.getAllQuestionnaireIds()


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
            if (questionnaires.isEmpty()) return@let
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

    suspend fun deleteLocallyDeletedQuestionnaireWith(questionnaireId: String) =
        locallyDeletedQuestionnaireDao.deleteLocallyDeletedQuestionnaireWith(questionnaireId)

    //LOCALLY DELETED FILLED QUESTIONNAIRE
    suspend fun getLocallyClearedQuestionnaireIds() = locallyClearedQuestionnaireDao.getLocallyDeletedFilledQuestionnaireIds()

    suspend fun getAllLocallyClearedQuestionnaires(): List<MongoFilledQuestionnaire> {
        getLocallyClearedQuestionnaireIds().map { it.questionnaireId }.let { locallyDeleted ->
            return if (locallyDeleted.isEmpty()) emptyList()
            else {
                val foundCompleteQuestionnaires = findCompleteQuestionnairesWith(locallyDeleted)
                val deletedQuestionnaireIds = locallyDeleted - foundCompleteQuestionnaires.map { it.questionnaire.id }
                if (deletedQuestionnaireIds.isNotEmpty()) {
                    locallyClearedQuestionnaireDao.deleteLocallyDeletedFilledQuestionnairesWith(deletedQuestionnaireIds)
                }
                foundCompleteQuestionnaires.map { DataMapper.mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(it) }
            }
        }
    }

    //LOCALLY ANSWERED QUESTIONNAIRES
    suspend fun getLocallyAnsweredQuestionnaireIds() = locallyAnsweredQuestionnaireDao.getLocallyAnsweredQuestionnaireIds()

    suspend fun getAllLocallyAnsweredFilledQuestionnaires(): List<MongoFilledQuestionnaire> {
        getLocallyAnsweredQuestionnaireIds().map { it.questionnaireId }.let { locallyAnswered ->
            return if (locallyAnswered.isEmpty()) emptyList()
            else {
                val foundCompleteQuestionnaires = findCompleteQuestionnairesWith(locallyAnswered)
                val deletedQuestionnaireIds = locallyAnswered - foundCompleteQuestionnaires.map { it.questionnaire.id }
                if (deletedQuestionnaireIds.isNotEmpty()) {
                    locallyAnsweredQuestionnaireDao.deleteLocallyAnsweredQuestionnaireWith(deletedQuestionnaireIds)
                }
                foundCompleteQuestionnaires.map { DataMapper.mapRoomQuestionnaireToMongoFilledQuestionnaire(it) }
            }
        }
    }

    suspend fun isAnsweredQuestionnairePresent(questionnaireId: String) = locallyAnsweredQuestionnaireDao.isAnsweredQuestionnairePresent(questionnaireId) == 1

    suspend fun getLocallyAnsweredQuestionnaire(questionnaireId: String) = locallyAnsweredQuestionnaireDao.getLocallyAnsweredQuestionnaire(questionnaireId)

    //LOCALLY DELETED USERS
    suspend fun getAllLocallyDeletedUserIds() = locallyDeletedUserDao.getAllLocallyDeletedUserIds()




    //FACULTY
    suspend fun getFacultyIdsWithTimestamp() = facultyDao.getFacultyIdsWithTimestamp()

    val allFacultiesFlow = facultyDao.allFacultiesFlow

    fun getCoursesOfStudiesForFacultyAlt(facultyId: String) = facultyDao.getCoursesOfStudiesForFacultyAlt(facultyId)

//    fun getCoursesOfStudiesForFaculty(facultyId: String) = facultyDao.getCoursesOfStudiesForFaculty(facultyId)


    //COURSE OF STUDIES
    suspend fun getCourseOfStudiesIdsWithTimestamp() = courseOfStudiesDao.getCourseOfStudiesIdsWithTimestamp()

    val allCoursesOfStudiesFlow= courseOfStudiesDao.allCoursesOfStudiesFlow

}