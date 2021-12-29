package com.example.quizapp.model.ktor

import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.databases.mongodb.documents.MongoCourseOfStudies
import com.example.quizapp.model.databases.mongodb.documents.MongoFaculty
import com.example.quizapp.model.databases.mongodb.documents.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.MongoQuestionnaire
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.datastore.datawrappers.ManageUsersOrderBy
import com.example.quizapp.model.datastore.datawrappers.RemoteQuestionnaireOrderBy
import com.example.quizapp.model.ktor.apiclasses.*
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendRepository @Inject constructor(
    private val userApi: UserApi,
    private val questionnaireApi: QuestionnaireApi,
    private val filledQuestionnaireApi: FilledQuestionnaireApi,
    private val facultyApi: FacultyApi,
    private val courseOfStudiesApi: CourseOfStudiesApi
) {

    // USER
    suspend fun loginUser(userName: String, password: String) = userApi.loginUser(userName, password)

    suspend fun registerUser(userName: String, password: String) = userApi.registerUser(userName, password)

    suspend fun getPagedAuthors(limit: Int = PagingConfigValues.DEFAULT_PAGE_SIZE, page: Int, searchString: String) = userApi.getPagedAuthors(limit, page, searchString)

    suspend fun updateUsername(newUserName: String) = userApi.updateUsername(newUserName)

    suspend fun deleteSelf() = userApi.deleteSelf()

    suspend fun updateUserRole(userId: String, newRole: Role) = userApi.updateUserRole(userId, newRole)

    suspend fun getPagedUsersAdmin(
        limit: Int = PagingConfigValues.DEFAULT_PAGE_SIZE,
        page: Int, searchString: String,
        roles: Set<Role>,
        orderBy: ManageUsersOrderBy,
        ascending: Boolean
    ) = userApi.getPagedUsersAdmin(limit, page, searchString, roles, orderBy, ascending)

    suspend fun deleteUser(userId: String) = userApi.deleteUser(userId)

    suspend fun syncUserData(userId: String) = userApi.syncUserData(userId)

    suspend fun createUser(userName: String, password: String, role: Role) = userApi.createUser(userName, password, role)

    suspend fun updateUserPassword(newPassword: String) = userApi.updateUserPassword(newPassword)



    // QUESTIONNAIRES
    suspend fun insertQuestionnaire(mongoQuestionnaire: MongoQuestionnaire) =
        questionnaireApi.insertQuestionnaires(listOf(mongoQuestionnaire))

    suspend fun insertQuestionnaires(mongoQuestionnaires: List<MongoQuestionnaire>) =
        questionnaireApi.insertQuestionnaires(mongoQuestionnaires)

    suspend fun getQuestionnairesForSyncronization(
        syncedQuestionnaireIdsWithTimestamp: List<QuestionnaireIdWithTimestamp>,
        unsyncedQuestionnaireIds: List<String>,
        questionnairesToDelete: List<LocallyDeletedQuestionnaire>
    ) = questionnaireApi.getQuestionnairesForSyncronization(syncedQuestionnaireIdsWithTimestamp, unsyncedQuestionnaireIds, questionnairesToDelete)

    suspend fun deleteQuestionnaire(questionnaireIds: List<String>) = questionnaireApi.deleteQuestionnaire(questionnaireIds)

    suspend fun getPagedQuestionnaires(
        limit: Int = PagingConfigValues.DEFAULT_PAGE_SIZE,
        page: Int,
        searchString: String,
        questionnaireIdsToIgnore: List<String>,
        facultyIds: List<String>,
        courseOfStudiesIds: List<String>,
        authorIds: List<String>,
        remoteQuestionnaireOrderBy: RemoteQuestionnaireOrderBy,
        ascending: Boolean
    ) = questionnaireApi.getPagedQuestionnaires(
        limit = limit,
        page = page,
        searchString = searchString,
        questionnaireIdsToIgnore = questionnaireIdsToIgnore,
        facultyIds = facultyIds,
        courseOfStudiesIds = courseOfStudiesIds,
        authorIds = authorIds,
        remoteQuestionnaireOrderBy = remoteQuestionnaireOrderBy,
        ascending = ascending
    )

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

    suspend fun insertFaculty(faculty: MongoFaculty) = facultyApi.insertFaculty(faculty)

    suspend fun deleteFaculty(facultyId: String) = facultyApi.deleteFaculty(facultyId)



    // COURSE OF STUDIES
    suspend fun getCourseOfStudiesSynchronizationData(localCourseIfStudiesIdsWithTimeStamp: List<CourseOfStudiesIdWithTimeStamp>) =
        courseOfStudiesApi.getCourseOfStudiesSynchronizationData(localCourseIfStudiesIdsWithTimeStamp)

    suspend fun insertCourseOfStudies(courseOfStudies: MongoCourseOfStudies) = courseOfStudiesApi.insertCourseOfStudies(courseOfStudies)

    suspend fun deleteCourseOfStudies(courseOfStudiesId: String) = courseOfStudiesApi.deleteCourseOfStudies(courseOfStudiesId)

}