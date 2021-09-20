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
            localRepo.insert(questionnaire)?.let { questionnaireId ->
                generateQuestions(maxQuestionsPerQuestionnaire, questionnaireId).forEach { question ->
                    localRepo.insert(question)?.let { questionId ->
                        localRepo.insert(generateAnswers(maxAnswersPerQuestion, questionId).toList())
                    }
                }
            }
        }
    }

    private fun generateQuestionnaires(questionnaireAmount: Int) = Array(questionnaireAmount) {
        Questionnaire(0, "Questionnaire $it", randomAuthorName, randomFaculty, randomCourseOfStudies, randomSubject)
    }

    private fun generateQuestions(maxQuestionsPerQuestionnaire: Int, questionnaireId : Long) = Array(Random.nextInt(maxQuestionsPerQuestionnaire) + 1) {
        Question(0, questionnaireId,"Question $it", Random.nextBoolean(), it)
    }

    private fun generateAnswers(maxAnswersPerQuestion: Int, questionId : Long) = Array(Random.nextInt(maxAnswersPerQuestion - 2) + 2) {
        Answer(0, questionId,"Answer $it", Random.nextBoolean(), false)
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