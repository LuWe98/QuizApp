package com.example.quizapp.model.databases.mongodb.documents.questionnairefilled

import com.example.quizapp.model.databases.room.entities.sync.LocallyFilledQuestionnaireToUpload
import kotlinx.serialization.Serializable

@Serializable
data class MongoFilledQuestionnaire(
    val questionnaireId : String,
    val userId : String,
    val questions : List<MongoFilledQuestion> = emptyList()
) {

    val allSelectedAnswerIds get() = questions.flatMap { it.selectedAnswerIds }

    fun isAnswerSelected(questionId : String, answerId : String) = questions.first { it.questionId == questionId }.selectedAnswerIds.contains(answerId)

    fun isAnswerSelected(answerId: String) = allSelectedAnswerIds.contains(answerId)

    val asLocallyFilledQuestionnaireToUpload get() = LocallyFilledQuestionnaireToUpload(questionnaireId)

}