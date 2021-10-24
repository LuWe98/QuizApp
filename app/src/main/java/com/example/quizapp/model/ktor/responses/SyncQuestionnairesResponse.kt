package com.example.quizapp.model.ktor.responses

import com.example.quizapp.model.mongodb.documents.questionnairefilled.MongoFilledQuestionnaire
import com.example.quizapp.model.mongodb.documents.questionnaire.MongoQuestionnaire
import kotlinx.serialization.Serializable

@Serializable
data class SyncQuestionnairesResponse(
    val mongoQuestionnaires: List<MongoQuestionnaire>,
    val mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>,
    val questionnaireIdsToUnsync: List<String>,
    val responseType: SyncQuestionnairesResponseType
) {
    enum class  SyncQuestionnairesResponseType {
        SUCCESSFUL,
        ERROR
    }
}