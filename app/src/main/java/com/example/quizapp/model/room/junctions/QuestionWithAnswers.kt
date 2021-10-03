package com.example.quizapp.model.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuestionWithAnswers(
    @Embedded
    var question: Question,
    @Relation(entity = Answer::class, entityColumn = "questionId", parentColumn = "id")
    var answers: List<Answer>
) : Parcelable {

    val answersAmount: Int get() = answers.size

    val isAnsweredCorrectly : Boolean get() = answers.all { it.isAnswerCorrect == it.isAnswerSelected }

    val isAnswered : Boolean get() = answers.any { it.isAnswerSelected }

    val answersSortedByPosition get() = answers.sortedBy { it.answerPosition }

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<QuestionWithAnswers> { old, new ->  old.question.id == new.question.id}

        fun createEmptyQuestionWithAnswers() : QuestionWithAnswers {
            val randomPosition = System.currentTimeMillis().toString().apply { substring(length - 8) }.toInt()
            val emptyQuestion = Question(questionnaireId =  "", questionText =  "", isMultipleChoice =  true, questionPosition = randomPosition)
            return QuestionWithAnswers(emptyQuestion, mutableListOf())
        }
    }
}