package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire.MongoFilledQuestionnaire
import com.example.quizapp.model.ktor.requests.BackendRequest
import com.example.quizapp.model.ktor.requests.BackendRequest.*
import com.example.quizapp.model.ktor.responses.BackendResponse
import com.example.quizapp.model.ktor.responses.BackendResponse.*
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilledQuestionnaireApi @Inject constructor(
    private val client: HttpClient
)  {

    suspend fun insertEmptyFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) : InsertFilledQuestionnaireResponse =
        client.post("/questionnaire/filled/insert") {
            body = InsertFilledQuestionnaireRequest(true, mongoFilledQuestionnaire)
        }

    suspend fun insertFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) : InsertFilledQuestionnaireResponse =
        client.post("/questionnaire/filled/insert") {
            body = InsertFilledQuestionnaireRequest(false, mongoFilledQuestionnaire)
        }

    suspend fun deleteFilledQuestionnaire(userId: String, questionnaireIds: List<String>) : DeleteFilledQuestionnaireResponse =
        client.delete("/questionnaire/filled/delete"){
            body = DeleteFilledQuestionnaireRequest(userId, questionnaireIds)
        }
}