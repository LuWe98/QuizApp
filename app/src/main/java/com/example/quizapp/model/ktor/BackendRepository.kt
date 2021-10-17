package com.example.quizapp.model.ktor

import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.ktor.apiclasses.FilledQuestionnaireApi
import com.example.quizapp.model.ktor.apiclasses.QuestionnaireApi
import com.example.quizapp.model.ktor.apiclasses.UserApi
import com.example.quizapp.model.mongodb.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.mongodb.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.mongodb.documents.user.Role
import com.example.quizapp.model.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendRepository @Inject constructor(
    private val userApi: UserApi,
    private val questionnaireApi: QuestionnaireApi,
    private val filledQuestionnaireApi: FilledQuestionnaireApi
) {

    // USER
    suspend fun loginUser(userName: String, password: String) = userApi.loginUser(userName, password)

    suspend fun registerUser(userName: String, password: String, courseOfStudies: String) = userApi.registerUser(userName, password, courseOfStudies)

    suspend fun updateUsername(userId: String, newUserName: String) = userApi.updateUsername(userId, newUserName)

    suspend fun updateUserRole(userId: String, newRole: Role) = userApi.updateUserRole(userId, newRole)

    suspend fun deleteUser(userId: String) = userApi.deleteUser(userId)

    suspend fun getPagedUsers(limit: Int, page: Int, searchString: String) = userApi.getPagedUsers(limit, page, searchString)


    // QUESTIONNAIRES
    suspend fun getQuestionnairesOfUser() = questionnaireApi.getQuestionnaireOfUser()

    suspend fun getAllQuestionnaires() = questionnaireApi.getAllQuestionnaires()

    suspend fun insertQuestionnaire(mongoQuestionnaire: MongoQuestionnaire) = questionnaireApi.insertQuestionnaire(mongoQuestionnaire)

    suspend fun insertQuestionnaire(completeCompleteQuestionnaire: CompleteQuestionnaireJunction) =
        questionnaireApi.insertQuestionnaire(DataMapper.mapSqlEntitiesToMongoEntity(completeCompleteQuestionnaire))

    suspend fun getQuestionnairesForSyncronization(
        syncedQuestionnaireIdsWithTimestamp: List<QuestionnaireIdWithTimestamp>,
        unsyncedQuestionnaireIds: List<String>,
        questionnairesToDelete: List<LocallyDeletedQuestionnaire>
    ) = questionnaireApi.getQuestionnairesForSyncronization(syncedQuestionnaireIdsWithTimestamp, unsyncedQuestionnaireIds, questionnairesToDelete)

    suspend fun deleteQuestionnaire(questionnaireIds: List<String>) = questionnaireApi.deleteQuestionnaire(questionnaireIds)

    suspend fun getPagedQuestionnaires(limit: Int, page: Int, searchString: String) = questionnaireApi.getPagedQuestionnaires(limit, page, searchString)


    // FILLED QUESTIONNAIRED
    suspend fun insertEmptyFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) =
        filledQuestionnaireApi.insertEmptyFilledQuestionnaire(mongoFilledQuestionnaire)

    suspend fun insertFilledQuestionnaires(mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>) =
        filledQuestionnaireApi.insertFilledQuestionnaires(mongoFilledQuestionnaires)

    suspend fun insertFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) =
        filledQuestionnaireApi.insertFilledQuestionnaire(mongoFilledQuestionnaire)

    suspend fun deleteFilledQuestionnaire(userId: String, questionnaireIds: List<String>) =
        filledQuestionnaireApi.deleteFilledQuestionnaire(userId, questionnaireIds)

}