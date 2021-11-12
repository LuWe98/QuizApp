package com.example.quizapp.model.databases.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.utils.DiffCallbackUtil
import kotlinx.parcelize.Parcelize

@Parcelize
data class CompleteQuestionnaire(
    @Embedded
    var questionnaire: Questionnaire,
    @Relation(
        entity = Question::class,
        entityColumn = Question.QUESTIONNAIRE_ID_COLUMN,
        parentColumn = Questionnaire.ID_COLUMN
    )
    var questionsWithAnswers: MutableList<QuestionWithAnswers>,
    @Relation(
        entity = Faculty::class,
        entityColumn = Faculty.ID_COLUMN,
        parentColumn = Questionnaire.FACULTY_ID_COLUMN
    )
    var faculty: Faculty?,
    @Relation(
        entity = CourseOfStudies::class,
        entityColumn = CourseOfStudies.ID_COLUMN,
        parentColumn = Questionnaire.COURSE_OF_STUDIES_ID_COLUMN
    )
    var courseOfStudies: CourseOfStudies?,
) : Parcelable {

    val allQuestions: List<Question> get() = questionsWithAnswers.map(QuestionWithAnswers::question)

    val allAnswers: List<Answer> get() = questionsWithAnswers.flatMap(QuestionWithAnswers::answers)

    val allSelectedAnswerIds: List<String> get() = allAnswers.filter(Answer::isAnswerSelected).map(Answer::id)

    fun isAnswerSelected(answerId: String) = allAnswers.firstOrNull { it.id == answerId }?.isAnswerSelected ?: false

    fun getQuestionWithAnswers(questionId: String): QuestionWithAnswers = questionsWithAnswers.first { qwa -> qwa.question.id == questionId }

    fun isQuestionAnswered(questionId: String): Boolean = getQuestionWithAnswers(questionId).isAnswered

    fun isQuestionAnsweredCorrectly(questionId: String) : Boolean = getQuestionWithAnswers(questionId).isAnsweredCorrectly

    fun getAnswersForQuestion(questionId: String): List<Answer> = getQuestionWithAnswers(questionId).answers

    val questionsAmount get() = questionsWithAnswers.size

    val answeredQuestionsAmount get() = questionsWithAnswers.filter(QuestionWithAnswers::isAnswered).size

    val answeredQuestionsPercentage get() = (answeredQuestionsAmount * 100 / questionsAmount.toFloat()).toInt()

    val areAllQuestionsAnswered get() = questionsAmount == answeredQuestionsAmount

    val correctQuestionsAmount get() = questionsWithAnswers.filter(QuestionWithAnswers::isAnsweredCorrectly).size

    val correctQuestionsPercentage get() = (correctQuestionsAmount * 100 / questionsAmount.toFloat()).toInt()

    val areAllQuestionsCorrectlyAnswered get() = questionsWithAnswers.size == correctQuestionsAmount

    val asQuestionnaireIdWithTimestamp get() = questionnaire.asQuestionnaireIdWithTimeStamp

    val toQuizStatisticNumbers get() = QuizStatisticNumbers(questionsAmount, answeredQuestionsAmount, correctQuestionsAmount)

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<CompleteQuestionnaire> { old, new -> old.questionnaire.id == new.questionnaire.id }
    }


    data class QuizStatisticNumbers(
        val questionsAmount: Int,
        val answeredQuestionsAmount: Int,
        val correctQuestionsAmount: Int
    ) {
        val incorrectQuestionsAmount = answeredQuestionsAmount - correctQuestionsAmount

        val answeredQuestionsPercentage = (answeredQuestionsAmount * 100f / questionsAmount).toInt()
        val correctQuestionsPercentage = (correctQuestionsAmount * 100f / questionsAmount).toInt()
        val incorrectQuestionsPercentage = (incorrectQuestionsAmount * 100f / questionsAmount).toInt()
        val areAllQuestionsCorrectlyAnswered = questionsAmount == correctQuestionsAmount
        val areAllQuestionsAnswered = questionsAmount == answeredQuestionsAmount

        val incorrectQuestionsPercentageDiff get() = 100 - correctQuestionsPercentage
    }
}