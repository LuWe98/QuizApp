package com.example.quizapp.model.databases.room.junctions

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.quizapp.extensions.div
import com.example.quizapp.extensions.generateDiffItemCallback
import com.example.quizapp.model.databases.room.entities.*
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
    var questionsWithAnswers: List<QuestionWithAnswers>,
    @Relation(
        entity = CourseOfStudies::class,
        entityColumn = CourseOfStudies.ID_COLUMN,
        parentColumn = Questionnaire.ID_COLUMN,
        associateBy = Junction(QuestionnaireCourseOfStudiesRelation::class)
    )
    var coursesOfStudiesWithFaculties: List<CourseOfStudiesWithFaculties>,
) : Parcelable {

    val allQuestions: List<Question> get() = questionsWithAnswers.map(QuestionWithAnswers::question)

    val allAnswers: List<Answer> get() = questionsWithAnswers.flatMap(QuestionWithAnswers::answers)

    val allSelectedAnswerIds: List<String> get() = allAnswers.filter(Answer::isAnswerSelected).map(Answer::id)

    fun isAnswerSelected(answerId: String) = allAnswers.firstOrNull { it.id == answerId }?.isAnswerSelected ?: false

    fun getQuestionWithAnswers(questionId: String): QuestionWithAnswers = questionsWithAnswers.first { qwa -> qwa.question.id == questionId }

    fun isQuestionAnswered(questionId: String): Boolean = getQuestionWithAnswers(questionId).isAnswered

    fun isQuestionAnsweredCorrectly(questionId: String): Boolean = getQuestionWithAnswers(questionId).isAnsweredCorrectly

    private val questionsAmount get() = questionsWithAnswers.size

    val hasQuestions get() = questionsAmount != 0

    private val answeredQuestionsAmount get() = questionsWithAnswers.filter(QuestionWithAnswers::isAnswered).size

    val answeredQuestionsPercentage get() = (answeredQuestionsAmount * 100 / questionsAmount.toFloat()).toInt()

    val areAllQuestionsAnswered get() = questionsAmount == answeredQuestionsAmount

    val isAnyQuestionNotAnswered get() = !areAllQuestionsAnswered

    private val correctQuestionsAmount get() = questionsWithAnswers.filter(QuestionWithAnswers::isAnsweredCorrectly).size

    val correctQuestionsPercentage get() = (correctQuestionsAmount * 100 / questionsAmount.toFloat()).toInt()

    val areAllQuestionsCorrectlyAnswered get() = questionsWithAnswers.size == correctQuestionsAmount

    val isAnyQuestionNotCorrectlyAnswered get() = !areAllQuestionsCorrectlyAnswered

    val asQuestionnaireIdWithTimestamp get() = questionnaire.asQuestionnaireIdWithTimeStamp

    val toQuizStatisticNumbers get() = QuizStatisticNumbers(questionsAmount, answeredQuestionsAmount, correctQuestionsAmount)




    //COS AND FACULTY STUFF
    val asQuestionnaireCourseOfStudiesRelations get() = run {
        coursesOfStudiesWithFaculties.map {
            QuestionnaireCourseOfStudiesRelation(questionnaire.id, it.courseOfStudies.id)
        }
    }

    val allCoursesOfStudies get() = coursesOfStudiesWithFaculties
        .map(CourseOfStudiesWithFaculties::courseOfStudies)
        .distinctBy(CourseOfStudies::id)

    val allFaculties get() = coursesOfStudiesWithFaculties
        .flatMap(CourseOfStudiesWithFaculties::faculties)
        .distinctBy(Faculty::id)

    val courseOfStudiesAbbreviations get() = run {
        allCoursesOfStudies.map(CourseOfStudies::abbreviation).reduceOrNull { acc, s -> "$acc, $s" } ?: ""
    }

    val facultiesAbbreviations get() = run {
        allFaculties.map(Faculty::abbreviation).reduceOrNull { acc, s -> "$acc, $s" } ?: ""
    }



    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(CompleteQuestionnaire::questionnaire / Questionnaire::id)
    }

    data class QuizStatisticNumbers(
        val questionsAmount: Int,
        val answeredQuestionsAmount: Int,
        val correctQuestionsAmount: Int
    ) {
        val incorrectQuestionsAmount = answeredQuestionsAmount - correctQuestionsAmount
        val areAllQuestionsCorrectlyAnswered = questionsAmount == correctQuestionsAmount
        val areAllQuestionsAnswered = questionsAmount == answeredQuestionsAmount

        val answeredQuestionsPercentage = (answeredQuestionsAmount * 100f / questionsAmount).toInt()
        val correctQuestionsPercentage = (correctQuestionsAmount * 100f / questionsAmount).toInt()
        val incorrectQuestionsPercentage = if (answeredQuestionsPercentage == 100) 100 - correctQuestionsPercentage else (incorrectQuestionsAmount * 100f / questionsAmount).toInt()
        val incorrectQuestionsPercentageDiff get() = 100 - correctQuestionsPercentage

        val hasQuestions get() = questionsAmount != 0
    }
}