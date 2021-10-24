package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class InsertFilledQuestionnairesResponse(
    val notInsertedQuestionnaireIds: List<String> = emptyList(),
    val responseType: InsertFilledQuestionnairesResponseType
) {
    enum class  InsertFilledQuestionnairesResponseType {
        SUCCESSFUL,
        ERROR
    }
}
