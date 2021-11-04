package com.example.quizapp.model.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class InsertQuestionnairesResponse(
    val responseType: InsertQuestionnairesResponseType
) {
    enum class  InsertQuestionnairesResponseType {
        SUCCESSFUL,
        NOT_ACKNOWLEDGED,
        ERROR
    }
}
