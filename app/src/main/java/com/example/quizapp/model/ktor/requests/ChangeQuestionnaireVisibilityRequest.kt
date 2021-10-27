package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.mongodb.documents.questionnaire.QuestionnaireVisibility
import kotlinx.serialization.Serializable

@Serializable
data class ChangeQuestionnaireVisibilityRequest(
    val questionnaireId: String,
    val newVisibility: QuestionnaireVisibility
)
