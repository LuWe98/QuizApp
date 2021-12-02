package com.example.quizapp.model.ktor

import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.ktor.apiclasses.*
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.databases.QuestionnaireVisibility
import com.example.quizapp.model.databases.mongodb.documents.faculty.MongoCourseOfStudies
import com.example.quizapp.model.databases.mongodb.documents.faculty.MongoFaculty
import com.example.quizapp.model.databases.mongodb.documents.questionnairefilled.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.datastore.datawrappers.BrowsableOrderBy
import com.example.quizapp.model.datastore.datawrappers.ManageUsersOrderBy
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



        // QUESTIONNAIRES
    suspend fun insertQuestionnaire(completeCompleteQuestionnaire: CompleteQuestionnaire) =
        insertQuestionnaire(DataMapper.mapRoomQuestionnaireToMongoQuestionnaire(completeCompleteQuestionnaire))

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
        browsableOrderBy: BrowsableOrderBy,
        ascending: Boolean
    ) = questionnaireApi.getPagedQuestionnaires(
        limit = limit,
        page = page,
        searchString = searchString,
        questionnaireIdsToIgnore = questionnaireIdsToIgnore,
        facultyIds = facultyIds,
        courseOfStudiesIds = courseOfStudiesIds,
        authorIds = authorIds,
        browsableOrderBy = browsableOrderBy,
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