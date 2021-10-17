package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class InsertFilledQuestionnairesResponse(
    val isSuccessful: Boolean,
    val notInsertedQuestionnaireIds: List<String> = emptyList(),
    val responseType: InsertFilledQuestionnairesResponseType
) {
    enum class  InsertFilledQuestionnairesResponseType {
        SUCCESSFUL,
        ERROR
    }
}
