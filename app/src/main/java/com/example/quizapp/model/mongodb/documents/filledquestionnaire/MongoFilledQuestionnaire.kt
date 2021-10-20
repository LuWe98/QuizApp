package com.example.quizapp.model.mongodb.documents.filledquestionnaire

import kotlinx.serialization.Serializable

@Serializable
data class MongoFilledQuestionnaire(
    var questionnaireId : String,
    var userId : String,
    var questions : List<MongoFilledQuestion> = emptyList()
) {

    val allSelectedAnswerIds get() = questions.flatMap { it.selectedAnswerIds }

    fun isAnswerSelected(questionId : String, answerId : String) = questions.first { it.questionId == questionId }.selectedAnswerIds.contains(answerId)

    fun isAnswerSelected(answerId: String) = allSelectedAnswerIds.contains(answerId)

}