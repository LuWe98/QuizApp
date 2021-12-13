package com.example.quizapp.model.ktor.responses

import com.example.quizapp.model.databases.mongodb.documents.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.MongoQuestionnaire
import kotlinx.serialization.Serializable

@Serializable
data class SyncQuestionnairesResponse(
    val mongoQuestionnaires: List<MongoQuestionnaire>,
    val mongoFilledQuestionnaires: List<MongoFilledQuestionnaire>,
    val questionnaireIdsToUnsync: List<String>
) {

    fun isEmpty() = mongoQuestionnaires.isEmpty() && mongoFilledQuestionnaires.isEmpty() && questionnaireIdsToUnsync.isEmpty()

}