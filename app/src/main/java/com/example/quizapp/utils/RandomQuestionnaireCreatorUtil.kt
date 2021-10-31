package com.example.quizapp.utils

import com.example.quizapp.model.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.room.entities.questionnaire.Answer
import com.example.quizapp.model.room.entities.questionnaire.Question
import com.example.quizapp.model.room.entities.questionnaire.Questionnaire
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
        Questionnaire(title = "Questionnaire $it", authorInfo = randomAuthor, courseOfStudies = "WIB", faculty = "WI", subject = randomSubject)
    }

    private fun generateQuestions(min: Int, max: Int, questionnaireId: String) = Array(Random.nextInt(max + 1 - min) + min) {
        Question(questionnaireId = questionnaireId, questionText = "Question $it", isMultipleChoice = Random.nextBoolean(), questionPosition = it)
    }

    private fun generateAnswers(min: Int, max: Int, questionId: String) = Array(Random.nextInt(max + 1 - min) + min) {
        Answer(questionId = questionId, answerText = "Answer $it", isAnswerCorrect = Random.nextBoolean(), isAnswerSelected = false)
    }

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

    private val randomAuthor
        get() = if (Random.nextBoolean()) AuthorInfo("615b295b6f9d372bf28212bb", "Luca")
        else AuthorInfo("615b2ee78d92a10ed1292a84", "Sasi")
}