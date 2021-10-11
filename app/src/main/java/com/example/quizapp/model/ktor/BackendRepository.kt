package com.example.quizapp.model.ktor

import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.ktor.apiclasses.FilledQuestionnaireApi
import com.example.quizapp.model.ktor.apiclasses.QuestionnaireApi
import com.example.quizapp.model.ktor.apiclasses.UserApi
import com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.ktor.requests.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestionsAndAnswers
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

    suspend fun updateUser(userId: String, newUserName: String) = userApi.updateUser(userId, newUserName)

    suspend fun deleteUser(userId: String) = userApi.deleteUser(userId)

    suspend fun getPagedUsers(limit: Int, page: Int, searchString: String) = userApi.getPagedUsers(limit, page, searchString)




    // QUESTIONNAIRES
    suspend fun getQuestionnairesOfUser() = questionnaireApi.getQuestionnaireOfUser()

    suspend fun getAllQuestionnaires() = questionnaireApi.getAllQuestionnaires()

    suspend fun insertQuestionnaire(mongoQuestionnaire: MongoQuestionnaire) = questionnaireApi.insertQuestionnaire(mongoQuestionnaire)

    suspend fun insertQuestionnaire(completeQuestionnaire: QuestionnaireWithQuestionsAndAnswers) =
        questionnaireApi.insertQuestionnaire(DataMapper.mapSqlEntitiesToMongoEntity(completeQuestionnaire))

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

    suspend fun insertFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) =
        filledQuestionnaireApi.insertFilledQuestionnaire(mongoFilledQuestionnaire)

    suspend fun deleteFilledQuestionnaire(userId: String, questionnaireIds: List<String>) =
        filledQuestionnaireApi.deleteFilledQuestionnaire(userId, questionnaireIds)

}