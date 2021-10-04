package com.example.quizapp.utils

import com.example.quizapp.extensions.log
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import kotlin.random.Random

object RandomQuestionnaireCreatorUtil {

    fun generateAndInsertRandomData(
        questionnaireAmount: Int,
        minQuestionsPerQuestionnaire: Int,
        maxQuestionsPerQuestionnaire: Int,
        minAnswersPerQuestion: Int,
        maxAnswersPerQuestion: Int
    ): Triple<List<Questionnaire>, List<Question>, List<Answer>> {

        val questionnaires = mutableListOf<Questionnaire>().apply { addAll(generateQuestionnaires(questionnaireAmount)) }

        val questions = mutableListOf<Question>()
        questionnaires.forEach { questionnaire ->
            questions.addAll(generateQuestions(minQuestionsPerQuestionnaire, maxQuestionsPerQuestionnaire, questionnaire.id))
        }

        val answers = mutableListOf<Answer>()
        questions.forEach { question ->
            answers.addAll(generateAnswers(minAnswersPerQuestion, maxAnswersPerQuestion, question.id))
        }

        return Triple(questionnaires, questions, answers)
    }

    private fun generateQuestionnaires(questionnaireAmount: Int) = Array(questionnaireAmount) {
        Questionnaire(title = "Questionnaire $it", author = randomAuthorName, courseOfStudies = randomCourseOfStudies, faculty = randomFaculty, subject = randomSubject)
    }

    private fun generateQuestions(min: Int, max: Int, questionnaireId: String) = Array(Random.nextInt(max + 1 - min) + min) {
        Question(questionnaireId = questionnaireId, questionText = "Question $it", isMultipleChoice = Random.nextBoolean(), questionPosition = it)
    }

    private fun generateAnswers(min: Int, max: Int, questionId: String) = Array(Random.nextInt(max + 1 - min) + min) {
        Answer(questionId = questionId, answerText = "Answer $it", isAnswerCorrect = Random.nextBoolean(), isAnswerSelected = false)
    }

    private val randomAuthorName: String get() = authorNamePool[Random.nextInt(authorNamePool.size)]

    private val authorNamePool = mutableListOf(
        "Jonh",
        "Dion",
        "Luca",
        "Mattheis",
        "Sasi",
        "Ziekow",
        "Tamine",
        "Noll"
    )

    private val randomFaculty: String get() = facultyPool[Random.nextInt(facultyPool.size)]

    private val facultyPool = mutableListOf(
        "W",
        "WI",
        "DM",
        "GGS",
        "IT",
        "I",
        "MLS",
        "MME"
    )

    private val randomCourseOfStudies: String get() = courseOfStudiesPool[Random.nextInt(courseOfStudiesPool.size)]

    private val courseOfStudiesPool = mutableListOf(
        "WIB",
        "WNB",
        "BC",
        "IBS",
        "IBM",
        "IEB",
        "MBA",
        "BA"
    )

    private val randomSubject: String get() = subjectsPool[Random.nextInt(subjectsPool.size)]

    private val subjectsPool = mutableListOf(
        "DB",
        "RW",
        "FOMEDA",
        "PROMOD 1",
        "PROMOD 2",
        "BPS",
        "GPD",
        "Logistic"
    )
}