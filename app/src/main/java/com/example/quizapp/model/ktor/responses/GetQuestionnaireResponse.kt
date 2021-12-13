package com.example.quizapp.model.ktor.responses

import com.example.quizapp.model.databases.mongodb.documents.MongoQuestionnaire
import kotlinx.serialization.Serializable

@Serializable
data class GetQuestionnaireResponse(
    val responseType: GetQuestionnaireResponseType,
    val mongoQuestionnaire: MongoQuestionnaire? = null,
) {
    enum class GetQuestionnaireResponseType {
        SUCCESSFUL,
        QUESTIONNAIRE_NOT_FOUND,
    }
}