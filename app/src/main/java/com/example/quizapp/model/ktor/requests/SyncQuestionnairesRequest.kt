package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.dto.QuestionnaireIdWithTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class SyncQuestionnairesRequest(
    val syncedQuestionnaireIdsWithTimestamp : List<QuestionnaireIdWithTimestamp>,
    val unsyncedQuestionnaireIds: List<String>,
    val locallyDeletedQuestionnaireIds: List<String>
)
