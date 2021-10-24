package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.ktor.requests.DeleteFilledQuestionnaireRequest
import com.example.quizapp.model.ktor.requests.InsertFilledQuestionnaireRequest
import com.example.quizapp.model.ktor.requests.InsertFilledQuestionnairesRequest
import com.example.quizapp.model.ktor.responses.DeleteFilledQuestionnaireResponse
import com.example.quizapp.model.ktor.responses.InsertFilledQuestionnaireResponse
import com.example.quizapp.model.ktor.responses.InsertFilledQuestionnairesResponse
import com.example.quizapp.model.mongodb.documents.questionnairefilled.MongoFilledQuestionnaire
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

    suspend fun insertFilledQuestionnaires(mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>) : InsertFilledQuestionnairesResponse =
        client.post("/questionnaires/filled/insert") {
            body = InsertFilledQuestionnairesRequest(mongoFilledQuestionnaires)
        }

    suspend fun deleteFilledQuestionnaire(questionnaireIds: List<String>) : DeleteFilledQuestionnaireResponse =
        client.delete("/questionnaire/filled/delete"){
            body = DeleteFilledQuestionnaireRequest(questionnaireIds)
        }

}