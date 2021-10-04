package com.example.quizapp.model.room.junctions

import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.utils.DiffUtilHelper

data class QuestionnaireWithQuestionsAndAnswers(
    @Embedded
    var questionnaire: Questionnaire,
    @Relation(entity = Question::class, entityColumn = "questionnaireId", parentColumn = "id")
    var questionsWithAnswers: MutableList<QuestionWithAnswers>
) {

    val allQuestions: List<Question> get() = questionsWithAnswers.map { item -> item.question }

    val allAnswers: List<Answer> get() = questionsWithAnswers.flatMap { item -> item.answers }


    fun getQuestionWithAnswers(questionId: String): QuestionWithAnswers = questionsWithAnswers.first { qwa -> qwa.question.id == questionId }

    fun getAnswersForQuestion(questionId: String): List<Answer> = getQuestionWithAnswers(questionId).answers

    val questionsAmount: Int get() = questionsWithAnswers.size

    val answeredQuestionsAmount : Int get() = questionsWithAnswers.filter { it.isAnswered }.size

    val answeredQuestionsPercentage: Int get() = (answeredQuestionsAmount*100/questionsAmount.toFloat()).toInt()

    val areAllQuestionsAnswered get() = questionsAmount == answeredQuestionsAmount

    val correctQuestionsAmount get() = questionsWithAnswers.filter { it.isAnsweredCorrectly }.size

    val correctQuestionsPercentage get() = (correctQuestionsAmount*100/questionsAmount.toFloat()).toInt()

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<QuestionnaireWithQuestionsAndAnswers> { old, new -> old.questionnaire.id == new.questionnaire.id }
    }
}