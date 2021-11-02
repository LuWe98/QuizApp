package com.example.quizapp.model.ktor

import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.ktor.apiclasses.*
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.QuestionnaireVisibility
import com.example.quizapp.model.databases.mongodb.documents.questionnairefilled.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendRepository @Inject constructor(
    private val adminApi: AdminApi,
    private val userApi: UserApi,
    private val questionnaireApi: QuestionnaireApi,
    private val filledQuestionnaireApi: FilledQuestionnaireApi,
    private val facultyApi: FacultyApi,
    private val courseOfStudiesApi: CourseOfStudiesApi,
    private val subjectApi: SubjectApi
) {
    // ADMIN
    suspend fun updateUserRole(userId: String, newRole: Role) = adminApi.updateUserRole(userId, newRole)

    suspend fun getPagedUsers(limit: Int, page: Int, searchString: String) = adminApi.getPagedUsers(limit, page, searchString)

    suspend fun deleteUser(userId: String) = adminApi.deleteUser(userId)

    suspend fun deleteUsers(userIds: List<String>) = adminApi.deleteUsers(userIds)

    suspend fun syncUserData(userId: String) = userApi.syncUserData(userId)



    // USER
    suspend fun loginUser(userName: String, password: String) = userApi.loginUser(userName, password)

    suspend fun registerUser(userName: String, password: String) = userApi.registerUser(userName, password)

    suspend fun updateUsername(newUserName: String) = userApi.updateUsername(newUserName)

    suspend fun deleteSelf() = userApi.deleteSelf()



    // QUESTIONNAIRES
    suspend fun getQuestionnairesOfUser() = questionnaireApi.getQuestionnaireOfUser()

    suspend fun getAllQuestionnaires() = questionnaireApi.getAllQuestionnaires()

    suspend fun insertQuestionnaire(mongoQuestionnaire: MongoQuestionnaire) = questionnaireApi.insertQuestionnaire(mongoQuestionnaire)

    suspend fun insertQuestionnaire(completeCompleteQuestionnaire: CompleteQuestionnaire) =
        questionnaireApi.insertQuestionnaire(DataMapper.mapRoomQuestionnaireToMongoQuestionnaire(completeCompleteQuestionnaire))

    suspend fun getQuestionnairesForSyncronization(
        syncedQuestionnaireIdsWithTimestamp: List<QuestionnaireIdWithTimestamp>,
        unsyncedQuestionnaireIds: List<String>,
        questionnairesToDelete: List<LocallyDeletedQuestionnaire>
    ) = questionnaireApi.getQuestionnairesForSyncronization(syncedQuestionnaireIdsWithTimestamp, unsyncedQuestionnaireIds, questionnairesToDelete)

    suspend fun deleteQuestionnaire(questionnaireIds: List<String>) = questionnaireApi.deleteQuestionnaire(questionnaireIds)

    suspend fun getPagedQuestionnaires(limit: Int, page: Int, searchString: String, questionnaireIdsToIgnore: List<String>) =
        questionnaireApi.getPagedQuestionnaires(limit, page, searchString, questionnaireIdsToIgnore)

    suspend fun downloadQuestionnaire(questionnaireId: String) = questionnaireApi.downloadQuestionnaire(questionnaireId)

    suspend fun changeQuestionnaireVisibility(questionnaireId: String, newVisibility: QuestionnaireVisibility) =
        questionnaireApi.changeQuestionnaireVisibility(questionnaireId, newVisibility)

    suspend fun shareQuestionnaireWithUser(questionnaireId: String, userName: String, canEdit: Boolean) =
        questionnaireApi.shareQuestionnaireWithUser(questionnaireId, userName, canEdit)



    // FILLED QUESTIONNAIRES
    suspend fun insertEmptyFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) =
        filledQuestionnaireApi.insertEmptyFilledQuestionnaire(mongoFilledQuestionnaire)

    suspend fun insertFilledQuestionnaires(mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>) =
        filledQuestionnaireApi.insertFilledQuestionnaires(mongoFilledQuestionnaires)

    suspend fun insertFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) =
        filledQuestionnaireApi.insertFilledQuestionnaire(mongoFilledQuestionnaire)

    suspend fun deleteFilledQuestionnaire(questionnaireIds: List<String>) =
        filledQuestionnaireApi.deleteFilledQuestionnaire(questionnaireIds)



    // FACULTY
    suspend fun getFacultySynchronizationData(localFacultyIdsWithTimeStamp: List<FacultyIdWithTimeStamp>) =
        facultyApi.getFacultySynchronizationData(localFacultyIdsWithTimeStamp)



    // COURSE OF STUDIES
    suspend fun getCourseOfStudiesSynchronizationData(localCourseIfStudiesIdsWithTimeStamp: List<CourseOfStudiesIdWithTimeStamp>) =
        courseOfStudiesApi.getCourseOfStudiesSynchronizationData(localCourseIfStudiesIdsWithTimeStamp)



    // SUBJECT

}