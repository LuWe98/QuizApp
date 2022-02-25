package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.mongodb.documents.MongoFilledQuestionnaire
import com.example.quizapp.model.ktor.BackendResponse.*

interface FilledQuestionnaireApi {

    suspend fun insertEmptyFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) : InsertFilledQuestionnaireResponse

    suspend fun insertFilledQuestionnaire(mongoFilledQuestionnaire: MongoFilledQuestionnaire) : InsertFilledQuestionnaireResponse

    suspend fun insertFilledQuestionnaires(mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>) : InsertFilledQuestionnairesResponse

    suspend fun deleteFilledQuestionnaire(questionnaireIds: List<String>) : DeleteFilledQuestionnaireResponse

}