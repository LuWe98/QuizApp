package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.mongodb.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.ktor.requests.*
import com.example.quizapp.model.ktor.responses.*
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.QuestionnaireVisibility
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.browsable.MongoBrowsableQuestionnaire
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedQuestionnaire
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
        locallyDeletedQuestionnaire: List<LocallyDeletedQuestionnaire>
    ): SyncQuestionnairesResponse =
        client.post("/questionnaire/sync") {
            body = SyncQuestionnairesRequest(
                syncedQuestionnaireIdsWithTimestamp,
                unsyncedQuestionnaireIds,
                locallyDeletedQuestionnaire.map { it.questionnaireId }
            )
        }

    suspend fun deleteQuestionnaire(questionnaireIds: List<String>): DeleteQuestionnaireResponse =
        client.delete("/questionnaire/delete") {
            body = DeleteQuestionnaireRequest(questionnaireIds)
        }

    //TODO -> RÜCKGABEWERT ZU RESPONSE SEALED CLASS UMÄNDERN!
    suspend fun getPagedQuestionnaires(limit: Int, page: Int, searchString: String, questionnaireIdsToIgnore: List<String>) : List<MongoBrowsableQuestionnaire> =
        client.post("/questionnaires/paged"){
            body = GetPagedQuestionnairesRequest(limit, page, searchString, questionnaireIdsToIgnore)
        }

    suspend fun downloadQuestionnaire(questionnaireId: String) : GetQuestionnaireResponse =
        client.post("/questionnaire/download") {
            body = GetQuestionnaireRequest(questionnaireId)
        }

    suspend fun changeQuestionnaireVisibility(questionnaireId: String, newVisibility: QuestionnaireVisibility) : ChangeQuestionnaireVisibilityResponse =
        client.post("/questionnaire/visibility"){
            body = ChangeQuestionnaireVisibilityRequest(questionnaireId, newVisibility)
        }

    suspend fun shareQuestionnaireWithUser(questionnaireId: String, userName: String, canEdit: Boolean) : ShareQuestionnaireWithUserResponse =
        client.post("/questionnaire/share") {
            body = ShareQuestionnaireWithUserRequest(questionnaireId, userName, canEdit)
        }
}