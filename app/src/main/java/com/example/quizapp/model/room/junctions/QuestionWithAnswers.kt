package com.example.quizapp.model.room.junctions

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    val answersSortedByPosition get() = answers.sortedBy { it.position }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QuestionWithAnswers>(){
            override fun areItemsTheSame(oldItem: QuestionWithAnswers, newItem: QuestionWithAnswers) =
                oldItem.question.id == newItem.question.id
            override fun areContentsTheSame(oldItem: QuestionWithAnswers, newItem: QuestionWithAnswers) =
                oldItem == newItem
        }

        fun createEmptyQuestionWithAnswers() : QuestionWithAnswers {
            val randomId = System.currentTimeMillis()
            val randomPosition = randomId.toString().substring(randomId.toString().length - 8).toInt()
            val emptyQuestion = Question(questionnaireId =  "", questionText =  "", isMultipleChoice =  true, questionPosition = randomPosition)
            return QuestionWithAnswers(emptyQuestion, mutableListOf())
        }
    }
}