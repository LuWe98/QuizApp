package com.example.quizapp.model.ktor.mongo.documents.filledquestionnaire

import kotlinx.serialization.Serializable

@Serializable
data class MongoFilledQuestionnaire(
    var questionnaireId : String,
    var userId : String,
    var questions : List<MongoFilledQuestion> = emptyList()
) {

    fun isAnswerSelected(questionId : String, answerId : String) = questions.first { it.questionId == questionId }.selectedAnswerIds.contains(answerId)

    fun isAnswerSelected(answerId: String) = questions.flatMap { it.selectedAnswerIds }.contains(answerId)
}