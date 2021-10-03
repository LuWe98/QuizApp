package com.example.quizapp.model.mongo

import com.example.quizapp.model.mongo.questionnairedocument.MongoAnswer
import com.example.quizapp.model.mongo.questionnairedocument.MongoQuestion
import com.example.quizapp.model.mongo.questionnairedocument.MongoQuestionnaire
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestionsAndAnswers

object MongoMapper {

    fun mapMongoObjectToSqlEntities(mongoQuestionnaire: MongoQuestionnaire): QuestionnaireWithQuestionsAndAnswers {
        val questionnaire = Questionnaire(
            id = mongoQuestionnaire.id,
            author = mongoQuestionnaire.author,
            title = mongoQuestionnaire.title,
            courseOfStudies = mongoQuestionnaire.courseOfStudies,
            subject = "",
            faculty = ""
        )

        val questionsWithAnswers = mongoQuestionnaire.questions.map { question ->
            QuestionWithAnswers(Question(
                id = question.id,
                questionnaireId = mongoQuestionnaire.id,
                questionText = question.questionText,
                isMultipleChoice = question.isMultipleChoice,
                questionPosition = question.questionPosition
            ), question.answers.map { answer ->
                Answer(
                    id = answer.id,
                    questionId = question.id,
                    answerText = answer.answerText,
                    isAnswerCorrect = answer.isAnswerCorrect,
                    position = answer.answerPosition
                )
            })
        }.toMutableList()

        return QuestionnaireWithQuestionsAndAnswers(questionnaire, questionsWithAnswers)
    }


    fun mapSqlEntitiesToMongoObject(questionnaireWithQuestionsAndAnswers: QuestionnaireWithQuestionsAndAnswers) =
        questionnaireWithQuestionsAndAnswers.run { mapSqlEntitiesToMongoObject(questionnaire, questionsWithAnswers) }

    fun mapSqlEntitiesToMongoObject(questionnaire: Questionnaire, questions: List<Question>, answers: List<Answer>) =
        mapSqlEntitiesToMongoObject(questionnaire, questions.map { question ->
            QuestionWithAnswers(question = question, answers = answers.filter { answer -> answer.questionId == question.id })
        })

    fun mapSqlEntitiesToMongoObject(questionnaire: Questionnaire, questionsWithAnswers: List<QuestionWithAnswers>): MongoQuestionnaire {
        return MongoQuestionnaire(
            id = questionnaire.id,
            title = questionnaire.title,
            author = questionnaire.author,
            courseOfStudies = questionnaire.courseOfStudies
        ).apply {
            questions = questionsWithAnswers.map { qwa ->
                qwa.question.let { question ->
                    MongoQuestion(
                        id = question.id,
                        questionText = question.questionText,
                        isMultipleChoice = question.isMultipleChoice,
                        questionPosition = question.questionPosition
                    ).apply {
                        answers = qwa.answers.map { answer ->
                            MongoAnswer(
                                id = answer.id,
                                answerText = answer.answerText,
                                answerPosition = answer.position,
                                isAnswerCorrect = answer.isAnswerCorrect
                            )
                        }
                    }
                }
            }
        }
    }
}