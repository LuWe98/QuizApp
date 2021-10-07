package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.ktor.requests.BackendRequest.*
import com.example.quizapp.model.ktor.requests.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.ktor.responses.BackendResponse.*
import com.example.quizapp.model.room.entities.LocallyDeletedQuestionnaire
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionnaireApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun getQuestionnaireOfUser(): List<MongoQuestionnaire> =
        client.get("/questionnaire/user")

    suspend fun getAllQuestionnaires(): List<MongoQuestionnaire> =
        client.get("/questionnaires")


    suspend fun insertQuestionnaire(mongoQuestionnaire: MongoQuestionnaire): InsertQuestionnaireResponse =
        client.post("/questionnaire/insert") {
            body = InsertQuestionnaireRequest(mongoQuestionnaire)
        }

    suspend fun getQuestionnairesForSyncronization(
        syncedQuestionnaireIdsWithTimestamp: List<QuestionnaireIdWithTimestamp>,
        unsyncedQuestionnaireIds: List<String>,
        questionnairesToDelete: List<LocallyDeletedQuestionnaire>
    ): GetAllSyncedQuestionnairesResponse =
        client.post("/questionnaire/user/synced") {
            body = GetAllSyncedQuestionnairesRequest(
                syncedQuestionnaireIdsWithTimestamp,
                unsyncedQuestionnaireIds,
                questionnairesToDelete.map { it.questionnaireId }
            )
        }

    suspend fun deleteQuestionnaire(questionnaireIds: List<String>): DeleteQuestionnaireResponse =
        client.delete("/questionnaire/delete") {
            body = DeleteQuestionnaireRequest(questionnaireIds)
        }
}