package com.example.quizapp.model.room.junctions

import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.room.entities.questionnaire.Question
import com.example.quizapp.model.room.entities.questionnaire.Questionnaire
import com.example.quizapp.utils.DiffUtilHelper

data class QuestionnaireWithQuestions(
    @Embedded
    var questionnaire: Questionnaire,
    @Relation(entity = Question::class, entityColumn = "questionnaireId", parentColumn = "id")
    var questions: List<Question>
) {

    val questionsAmount: Int get() = questions.size

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<QuestionnaireWithQuestions> { old, new ->  old.questionnaire.id == new.questionnaire.id}
    }
}