package com.example.quizapp.model.ktor.responses

import com.example.quizapp.model.mongodb.documents.questionnaire.MongoQuestionnaire
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