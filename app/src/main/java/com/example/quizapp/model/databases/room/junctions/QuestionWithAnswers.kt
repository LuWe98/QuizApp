package com.example.quizapp.model.databases.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.extensions.div
import com.example.quizapp.extensions.generateDiffItemCallback
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuestionWithAnswers(
    @Embedded
    var question: Question,
    @Relation(
        entity = Answer::class,
        entityColumn = Answer.QUESTION_ID_COLUMN,
        parentColumn = Question.ID_COLUMN
    )
    var answers: List<Answer>
) : Parcelable {

    val isAnsweredCorrectly: Boolean get() = answers.all { it.isAnswerCorrect == it.isAnswerSelected }

    val isAnswered: Boolean get() = answers.any(Answer::isAnswerSelected)

    val answersSortedByPosition get() = answers.sortedBy(Answer::answerPosition)

    val selectedAnswerIds get() = answers.filter(Answer::isAnswerSelected).map(Answer::id)

    val shuffleSeedAdjusted get() = question.questionPosition + answers.size

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(QuestionWithAnswers::question / Question::id)

        fun createEmptyQuestionWithAnswers() = QuestionWithAnswers(
            Question(questionnaireId = "", questionText = "", isMultipleChoice = true, questionPosition = getTimeMillis().toInt()),
            emptyList()
        )
    }
}