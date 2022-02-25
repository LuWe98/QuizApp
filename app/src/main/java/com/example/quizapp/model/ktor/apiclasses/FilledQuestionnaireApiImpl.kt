package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.mongodb.documents.MongoFilledQuestionnaire
import com.example.quizapp.model.ktor.ApiPaths.FilledQuestionnairePaths
import com.example.quizapp.model.ktor.BackendRequest.*
import com.example.quizapp.model.ktor.BackendResponse.*
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilledQuestionnaireApiImpl @Inject constructor(
    private val client: HttpClient
) : FilledQuestionnaireApi {

    override suspend fun insertEmptyFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) : InsertFilledQuestionnaireResponse =
        client.post(FilledQuestionnairePaths.INSERT_SINGLE) {
            body = InsertFilledQuestionnaireRequest(true, mongoFilledQuestionnaire)
        }

    override suspend fun insertFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) : InsertFilledQuestionnaireResponse =
        client.post(FilledQuestionnairePaths.INSERT_SINGLE) {
            body = InsertFilledQuestionnaireRequest(false, mongoFilledQuestionnaire)
        }

    override suspend fun insertFilledQuestionnaires(mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>) : InsertFilledQuestionnairesResponse =
        client.post(FilledQuestionnairePaths.INSERT_MULTIPLE) {
            body = InsertFilledQuestionnairesRequest(mongoFilledQuestionnaires)
        }

    override suspend fun deleteFilledQuestionnaire(questionnaireIds: List<String>) : DeleteFilledQuestionnaireResponse =
        client.delete(FilledQuestionnairePaths.DELETE){
            body = DeleteFilledQuestionnaireRequest(questionnaireIds)
        }

}