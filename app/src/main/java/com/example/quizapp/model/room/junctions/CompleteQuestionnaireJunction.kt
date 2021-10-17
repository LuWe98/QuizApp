package com.example.quizapp.model.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class CompleteQuestionnaireJunction(
    @Embedded
    var questionnaire: Questionnaire,
    @Relation(entity = Question::class, entityColumn = "questionnaireId", parentColumn = "id")
    var questionsWithAnswers: MutableList<QuestionWithAnswers>
) : Parcelable {

    val allQuestions: List<Question> get() = questionsWithAnswers.map { item -> item.question }

    val allAnswers: List<Answer> get() = questionsWithAnswers.flatMap { item -> item.answers }

    fun isAnswerSelected(answerId : String) = allAnswers.firstOrNull { it.id == answerId }?.isAnswerSelected ?: false

    fun getQuestionWithAnswers(questionId: String): QuestionWithAnswers = questionsWithAnswers.first { qwa -> qwa.question.id == questionId }

    fun getAnswersForQuestion(questionId: String): List<Answer> = getQuestionWithAnswers(questionId).answers

    val questionsAmount get() = questionsWithAnswers.size

    val answeredQuestionsAmount get() = questionsWithAnswers.filter { it.isAnswered }.size

    val answeredQuestionsPercentage get() = (answeredQuestionsAmount*100/questionsAmount.toFloat()).toInt()

    val areAllQuestionsAnswered get() = questionsAmount == answeredQuestionsAmount

    val correctQuestionsAmount get() = questionsWithAnswers.filter { it.isAnsweredCorrectly }.size

    val correctQuestionsPercentage get() = (correctQuestionsAmount*100/questionsAmount.toFloat()).toInt()

    val areAllQuestionsCorrectlyAnswered get() = questionsWithAnswers.size == correctQuestionsAmount

    val asQuestionnaireIdWithTimestamp get() = questionnaire.asQuestionnaireIdWithTimeStamp

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<CompleteQuestionnaireJunction> { old, new -> old.questionnaire.id == new.questionnaire.id }
    }
}