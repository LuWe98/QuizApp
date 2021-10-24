package com.example.quizapp.model

import com.example.quizapp.model.mongodb.documents.questionnairefilled.MongoFilledQuestion
import com.example.quizapp.model.mongodb.documents.questionnairefilled.MongoFilledQuestionnaire
import com.example.quizapp.model.mongodb.documents.questionnaire.MongoAnswer
import com.example.quizapp.model.mongodb.documents.questionnaire.MongoQuestion
import com.example.quizapp.model.mongodb.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction

object DataMapper {

    fun mapMongoObjectToSqlEntities(
        mongoQuestionnaire: MongoQuestionnaire,
        mongoFilledQuestionnaire: MongoFilledQuestionnaire? = null
    ): CompleteQuestionnaireJunction {
        val questionnaire = Questionnaire(
            id = mongoQuestionnaire.id,
            authorInfo = mongoQuestionnaire.authorInfo,
            title = mongoQuestionnaire.title,
            lastModifiedTimestamp = mongoQuestionnaire.lastModifiedTimestamp,
            courseOfStudies = mongoQuestionnaire.courseOfStudies,
            subject = mongoQuestionnaire.subject,
            syncStatus = SyncStatus.SYNCED,
            faculty = "WI"
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
                    answerPosition = answer.answerPosition,
                    isAnswerSelected = mongoFilledQuestionnaire?.isAnswerSelected(question.id, answer.id) ?: false
                )
            })
        }.toMutableList()

        return CompleteQuestionnaireJunction(questionnaire, questionsWithAnswers)
    }


    fun mapSqlEntitiesToMongoEntity(
        completeCompleteQuestionnaire: CompleteQuestionnaireJunction
    ) = completeCompleteQuestionnaire.run { mapSqlEntitiesToMongoEntity(questionnaire, questionsWithAnswers) }

    fun mapSqlEntitiesToMongoEntity(
        questionnaire: Questionnaire,
        questions: List<Question>, answers: List<Answer>
    ) = mapSqlEntitiesToMongoEntity(questionnaire, questions.map { question ->
        QuestionWithAnswers(question = question, answers = answers.filter { answer -> answer.questionId == question.id })
    })

    fun mapSqlEntitiesToMongoEntity(
        questionnaire: Questionnaire,
        questionsWithAnswers: List<QuestionWithAnswers>
    ): MongoQuestionnaire {
        return MongoQuestionnaire(
            id = questionnaire.id,
            title = questionnaire.title,
            authorInfo = questionnaire.authorInfo,
            lastModifiedTimestamp = questionnaire.lastModifiedTimestamp,
            courseOfStudies = questionnaire.courseOfStudies,
            subject = questionnaire.subject
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
                                answerPosition = answer.answerPosition,
                                isAnswerCorrect = answer.isAnswerCorrect
                            )
                        }
                    }
                }
            }
        }
    }


    fun mapSqlEntitiesToFilledMongoEntity(
        completeCompleteQuestionnaire: CompleteQuestionnaireJunction
    ) = completeCompleteQuestionnaire.run { mapSqlEntitiesToFilledMongoEntity(questionnaire, questionsWithAnswers) }

    fun mapSqlEntitiesToFilledMongoEntity(
        questionnaire: Questionnaire,
        questions: List<Question>,
        answers: List<Answer>
    ) = mapSqlEntitiesToFilledMongoEntity(questionnaire, questions.map { question ->
        QuestionWithAnswers(question = question, answers = answers.filter { answer -> answer.questionId == question.id })
    })

    fun mapSqlEntitiesToFilledMongoEntity(
        questionnaire: Questionnaire,
        questionsWithAnswers: List<QuestionWithAnswers>
    ) = MongoFilledQuestionnaire(
        questionnaireId = questionnaire.id,
        userId = questionnaire.authorInfo.userId
    ).apply {
        questions = questionsWithAnswers.filter { it.isAnswered }.map { qwa ->
            qwa.question.let { question ->
                MongoFilledQuestion(
                    questionId = question.questionnaireId
                ).apply {
                    selectedAnswerIds = qwa.selectedAnswerIds
                }
            }
        }
    }

    fun mapSqlEntitiesToEmptyFilledMongoEntity(
        completeCompleteQuestionnaire: CompleteQuestionnaireJunction
    ) = completeCompleteQuestionnaire.run { mapSqlEntitiesToEmptyFilledMongoEntity(questionnaire) }

    fun mapSqlEntitiesToEmptyFilledMongoEntity(
        questionnaire: Questionnaire
    ) = MongoFilledQuestionnaire(
        questionnaireId = questionnaire.id,
        userId = questionnaire.authorInfo.userId
    )
}