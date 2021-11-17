package com.example.quizapp.model.databases.room

import androidx.room.withTransaction
import com.example.quizapp.extensions.div
import com.example.quizapp.model.databases.DataMapper
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
import com.example.quizapp.model.databases.room.entities.relations.QuestionnaireCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.entities.sync.*
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.ktor.status.SyncStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

//TODO -> Schauen ob Set vllt sinnvoller ist als List zu, zurückgeben, da anscheinden performance besser ist!
@Singleton
class LocalRepository @Inject constructor(
    private val localDatabase: LocalDatabase,
    private val questionnaireDao: QuestionnaireDao,
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val facultyDao: FacultyDao,
    private val courseOfStudiesDao: CourseOfStudiesDao,
    private val subjectDao: SubjectDao,
    private val questionnaireCourseOfStudiesRelationDao: QuestionnaireCourseOfStudiesRelationDao,
    private val facultyCourseOfStudiesRelationDao: FacultyCourseOfStudiesRelationDao,
    private val locallyDeletedQuestionnaireDao: LocallyDeletedQuestionnaireDao,
    private val locallyFilledQuestionnaireToUploadDao: LocallyFilledQuestionnaireToUploadDao,
    private val locallyDeletedUserDao: LocallyDeletedUserDao
) {

    suspend inline fun <reified T : EntityMarker> insert(entity: T) = getBaseDaoWith(T::class).insert(entity)

    suspend inline fun <reified T : EntityMarker> insert(entity: List<T>) = getBaseDaoWith(T::class).insert(entity)

    suspend inline fun <reified T : EntityMarker> insert(entity: Set<T>) = getBaseDaoWith(T::class).insert(entity)

    suspend inline fun <reified T : EntityMarker> update(entity: T) = getBaseDaoWith(T::class).update(entity)

    suspend inline fun <reified T : EntityMarker> update(entity: List<T>) = getBaseDaoWith(T::class).update(entity)

    suspend inline fun <reified T : EntityMarker> update(entity: Set<T>) = getBaseDaoWith(T::class).update(entity)

    suspend inline fun <reified T : EntityMarker> delete(entity: T) = getBaseDaoWith(T::class).delete(entity)

    suspend inline fun <reified T : EntityMarker> delete(entity: List<T>) = getBaseDaoWith(T::class).delete(entity)

    suspend inline fun <reified T : EntityMarker> delete(entity: Set<T>) = getBaseDaoWith(T::class).delete(entity)


    @Suppress("UNCHECKED_CAST")
    fun <T : EntityMarker> getBaseDaoWith(entity: KClass<T>) = (when (entity) {
        Answer::class -> answerDao
        Question::class -> questionDao
        Questionnaire::class -> questionnaireDao
        Faculty::class -> facultyDao
        CourseOfStudies::class -> courseOfStudiesDao
        Subject::class -> subjectDao
        QuestionnaireCourseOfStudiesRelation::class -> questionnaireCourseOfStudiesRelationDao
        FacultyCourseOfStudiesRelation::class -> facultyCourseOfStudiesRelationDao
        LocallyDeletedQuestionnaire::class -> locallyDeletedQuestionnaireDao
        LocallyFilledQuestionnaireToUpload::class -> locallyFilledQuestionnaireToUploadDao
        LocallyDeletedUser::class -> locallyDeletedUserDao
        else -> throw IllegalArgumentException("Entity DAO for entity could not be found! Did you add it to the 'getBaseDaoWith' Method?")
    } as BaseDao<T>)

    suspend fun deleteAllUserData() {
        localDatabase.withTransaction {
            questionnaireDao.deleteAll()
            locallyFilledQuestionnaireToUploadDao.deleteAll()
            locallyDeletedQuestionnaireDao.deleteAll()
            locallyDeletedUserDao.deleteAll()
        }
    }

    //QUESTIONNAIRE
    suspend fun insertCompleteQuestionnaire(completeQuestionnaire: CompleteQuestionnaire) {
        localDatabase.withTransaction {
            deleteQuestionnaireWith(completeQuestionnaire.questionnaire.id)
            insert(completeQuestionnaire.questionnaire)
            insert(completeQuestionnaire.allQuestions)
            insert(completeQuestionnaire.allAnswers)
            insert(completeQuestionnaire.asQuestionnaireCourseOfStudiesRelations)
        }
    }

    suspend fun insertCompleteQuestionnaires(completeQuestionnaire: List<CompleteQuestionnaire>) {
        localDatabase.withTransaction {
            deleteQuestionnairesWith(completeQuestionnaire.map(CompleteQuestionnaire::questionnaire / Questionnaire::id))
            insert(completeQuestionnaire.map(CompleteQuestionnaire::questionnaire))
            insert(completeQuestionnaire.flatMap(CompleteQuestionnaire::allQuestions))
            insert(completeQuestionnaire.flatMap(CompleteQuestionnaire::allAnswers))
            insert(completeQuestionnaire.flatMap(CompleteQuestionnaire::asQuestionnaireCourseOfStudiesRelations))
        }
    }

    private suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>) {
        delete(findQuestionnairesWith(questionnaireIds))
    }

    suspend fun getAllQuestionnaireIds() = questionnaireDao.getAllQuestionnaireIds()


    fun findAllCompleteQuestionnairesNotForUserFlow(userId: String) = questionnaireDao.findAllCompleteQuestionnairesNotForUserFlow(userId)

    fun findAllCompleteQuestionnairesForUserFlow(userId: String) = questionnaireDao.findAllCompleteQuestionnairesForUserFlow(userId)

    suspend fun findCompleteQuestionnaireWith(questionnaireId: String) = questionnaireDao.findCompleteQuestionnaireWith(questionnaireId)

    suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>) = questionnaireDao.findCompleteQuestionnairesWith(questionnaireIds)

    suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>, userId: String) = questionnaireDao.findCompleteQuestionnairesWith(questionnaireIds, userId)

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

    suspend fun setStatusToSynced(questionnaireIdsToSync: List<String>) = questionnaireDao.setStatusToSynced(questionnaireIdsToSync)

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



    //LOCALLY ANSWERED QUESTIONNAIRES
    suspend fun getAllLocallyFilledQuestionnairesToUpload(): List<MongoFilledQuestionnaire> {
        val locallyAnsweredQuestionnaireIds = locallyFilledQuestionnaireToUploadDao.getAllLocallyFilledQuestionnairesToUploadIds()

        locallyAnsweredQuestionnaireIds.map(LocallyFilledQuestionnaireToUpload::questionnaireId).let { locallyAnswered ->
            if (locallyAnswered.isEmpty()) return emptyList()

            val foundCompleteQuestionnaires = findCompleteQuestionnairesWith(locallyAnswered)

            (locallyAnswered - foundCompleteQuestionnaires.map(CompleteQuestionnaire::questionnaire / Questionnaire::id).toSet()).let { deletedQuestionnaireIds ->
                if (deletedQuestionnaireIds.isNotEmpty()) {
                    locallyFilledQuestionnaireToUploadDao.deleteLocallyFilledQuestionnaireToUploadWith(deletedQuestionnaireIds)
                }
            }

            return foundCompleteQuestionnaires.map(DataMapper::mapRoomQuestionnaireToMongoFilledQuestionnaire)
        }
    }

    suspend fun isLocallyFilledQuestionnaireToUploadPresent(questionnaireId: String) = locallyFilledQuestionnaireToUploadDao.isLocallyFilledQuestionnaireToUploadPresent(questionnaireId) == 1

    suspend fun getLocallyAnsweredQuestionnaire(questionnaireId: String) = locallyFilledQuestionnaireToUploadDao.getLocallyFilledQuestionnaireToUploadId(questionnaireId)



    //LOCALLY DELETED USERS
    suspend fun getAllLocallyDeletedUserIds() = locallyDeletedUserDao.getAllLocallyDeletedUserIds()



    //FACULTY
    suspend fun getFacultyIdsWithTimestamp() = facultyDao.getFacultyIdsWithTimestamp()

    val allFacultiesFlow = facultyDao.allFacultiesFlow

    suspend fun getFacultyWithCourseOfStudies(facultyId: String) = facultyDao.getFacultyWithCourseOfStudies(facultyId)

    suspend fun getFacultyWithId(facultyId: String) = facultyDao.getFacultyWithId(facultyId)

    suspend fun getFacultiesWithCourseOfStudiesIds(courseOfStudiesIds: List<String>) = facultyDao.getFacultiesWithCourseOfStudiesIds(courseOfStudiesIds)



    //COURSE OF STUDIES
    suspend fun getCourseOfStudiesIdsWithTimestamp() = courseOfStudiesDao.getCourseOfStudiesIdsWithTimestamp()

    val allCoursesOfStudiesFlow = courseOfStudiesDao.getAllCourseOfStudiesFlow()

    fun getCoursesOfStudiesFlowWithIds(courseOfStudiesIds: Set<String>) = courseOfStudiesDao.getCoursesOfStudiesFlowWithIds(courseOfStudiesIds)

    suspend fun deleteWhereAbbreviation(abb: String) = courseOfStudiesDao.deleteWhereAbbreviation(abb)

    suspend fun getCourseOfStudiesNameWithId(courseOfStudiesId: String) = courseOfStudiesDao.getCourseOfStudiesNameWithId(courseOfStudiesId)

    suspend fun getCoursesOfStudiesNameWithIds(courseOfStudiesIds: List<String>) = courseOfStudiesDao.getCoursesOfStudiesNameWithIds(courseOfStudiesIds)

    suspend fun getCourseOfStudiesWithId(courseOfStudiesId: String) = courseOfStudiesDao.getCourseOfStudiesWithId(courseOfStudiesId)



    //QUESTIONNAIRE COURSE OF STUDIES RELATION
    suspend fun getQuestionnaireCourseOfStudiesRelationWith(questionnaireId: String) =
        questionnaireCourseOfStudiesRelationDao.getQuestionnaireCourseOfStudiesRelationWith(questionnaireId)

}