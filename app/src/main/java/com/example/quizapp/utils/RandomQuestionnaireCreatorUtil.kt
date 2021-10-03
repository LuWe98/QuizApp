package com.example.quizapp.utils

import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import kotlin.random.Random

object RandomQuestionnaireCreatorUtil {

    suspend fun generateAndInsertRandomData(
        localRepo: LocalRepository,
        questionnaireAmount: Int,
        maxQuestionsPerQuestionnaire: Int,
        maxAnswersPerQuestion: Int
    ) {

        generateQuestionnaires(questionnaireAmount).forEach { questionnaire ->
            localRepo.insert(questionnaire)?.let {
                generateQuestions(maxQuestionsPerQuestionnaire, questionnaire.id).forEach { question ->
                    localRepo.insert(question)?.let {
                        localRepo.insert(generateAnswers(maxAnswersPerQuestion, question.id).toList())
                    }
                }
            }
        }
    }

    private fun generateQuestionnaires(questionnaireAmount: Int) = Array(questionnaireAmount) {
        Questionnaire(title =  "Questionnaire $it", author = randomAuthorName, courseOfStudies = randomCourseOfStudies, faculty = randomFaculty, subject = randomSubject)
    }

    private fun generateQuestions(maxQuestionsPerQuestionnaire: Int, questionnaireId : String) = Array(Random.nextInt(maxQuestionsPerQuestionnaire) + 1) {
        Question(questionnaireId = questionnaireId, questionText = "Question $it", isMultipleChoice = Random.nextBoolean(), questionPosition = it)
    }

    private fun generateAnswers(maxAnswersPerQuestion: Int, questionId : String) = Array(Random.nextInt(maxAnswersPerQuestion - 2) + 2) {
        Answer(questionId = questionId, answerText = "Answer $it", isAnswerCorrect = Random.nextBoolean(), isAnswerSelected = false)
    }

    private val randomAuthorName : String get() = authorNamePool[Random.nextInt(authorNamePool.size)]

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