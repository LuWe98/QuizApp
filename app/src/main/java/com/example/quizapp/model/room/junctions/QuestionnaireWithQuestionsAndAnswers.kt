package com.example.quizapp.model.room.junctions

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire

data class QuestionnaireWithQuestionsAndAnswers(
    @Embedded
    var questionnaire: Questionnaire,
    @Relation(entity = Question::class, entityColumn = "questionnaireId", parentColumn = "id")
    var questionsWithAnswers: MutableList<QuestionWithAnswers>
) {

    val questions: List<Question> get() = questionsWithAnswers.map { item -> item.question }

    fun getQuestionWithAnswers(questionId: String): QuestionWithAnswers = questionsWithAnswers.first { qwa -> qwa.question.id == questionId }

    fun getAnswersForQuestion(questionId: String): List<Answer> = getQuestionWithAnswers(questionId).answers

    val questionsAmount: Int get() = questionsWithAnswers.size

    val answeredQuestionsAmount : Int get() = questionsWithAnswers.filter { it.isAnswered }.size

    val answeredQuestionsPercentage: Int get() = (answeredQuestionsAmount*100/questionsAmount.toFloat()).toInt()

    val areAllQuestionsAnswered get() = questionsAmount == answeredQuestionsAmount

    val correctQuestionsAmount get() = questionsWithAnswers.filter { it.isAnsweredCorrectly }.size

    val correctQuestionsPercentage get() = (correctQuestionsAmount*100/questionsAmount.toFloat()).toInt()

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QuestionnaireWithQuestionsAndAnswers>(){
            override fun areItemsTheSame(oldItem: QuestionnaireWithQuestionsAndAnswers, newItem: QuestionnaireWithQuestionsAndAnswers) =
                oldItem.questionnaire.id == newItem.questionnaire.id
            override fun areContentsTheSame(oldItem: QuestionnaireWithQuestionsAndAnswers, newItem: QuestionnaireWithQuestionsAndAnswers) =
                oldItem == newItem
        }
    }
}