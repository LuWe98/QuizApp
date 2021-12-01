package com.example.quizapp.model.databases.room.junctions

import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.extensions.div
import com.example.quizapp.extensions.generateDiffItemCallback
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire

data class QuestionnaireWithQuestions(
    @Embedded
    var questionnaire: Questionnaire,
    @Relation(
        entity = Question::class,
        entityColumn = Question.QUESTIONNAIRE_ID_COLUMN,
        parentColumn = Questionnaire.ID_COLUMN
    )
    var questions: List<Question>
) {

    val questionsAmount: Int get() = questions.size

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(QuestionnaireWithQuestions::questionnaire / Questionnaire::id)
    }
}