package com.example.quizapp.model.databases

import com.example.quizapp.model.databases.mongodb.documents.questionnairefilled.MongoFilledQuestion
import com.example.quizapp.model.databases.mongodb.documents.questionnairefilled.MongoFilledQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.MongoAnswer
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.MongoQuestion
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.mongodb.documents.faculty.MongoFaculty
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaireJunction
import com.example.quizapp.model.databases.room.junctions.FacultyWithCoursesOfStudiesJunction

object DataMapper {

    fun mapMongoObjectToSqlEntities(
        mongoQuestionnaire: MongoQuestionnaire,
        mongoFilledQuestionnaire: MongoFilledQuestionnaire? = null
    ): CompleteQuestionnaireJunction {
        val questionnaire = Questionnaire(
            id = mongoQuestionnaire.id,
            title = mongoQuestionnaire.title,
            authorInfo = mongoQuestionnaire.authorInfo,
            faculty = mongoQuestionnaire.faculty,
            courseOfStudies = mongoQuestionnaire.courseOfStudies,
            subject = mongoQuestionnaire.subject,
            syncStatus = SyncStatus.SYNCED,
            questionnaireVisibility = mongoQuestionnaire.questionnaireVisibility,
            lastModifiedTimestamp = mongoQuestionnaire.lastModifiedTimestamp)

        val questionsWithAnswers = mongoQuestionnaire.questions.map { question ->
            QuestionWithAnswers(
                Question(
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
            faculty = questionnaire.faculty,
            courseOfStudies = questionnaire.courseOfStudies,
            subject = questionnaire.subject,
            questionnaireVisibility = questionnaire.questionnaireVisibility,
            lastModifiedTimestamp = questionnaire.lastModifiedTimestamp
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


    fun mapMongoFacultyToRoomEntity(mongoFaculty: MongoFaculty) : FacultyWithCoursesOfStudiesJunction {
        val faculty = Faculty(
            id = mongoFaculty.id,
            abbreviation = mongoFaculty.abbreviation,
            name = mongoFaculty.name,
            lastModifiedTimestamp = mongoFaculty.lastModifiedTimestamp
        )

        val coursesOfStudies = mongoFaculty.coursesOfStudies.map {
            CourseOfStudies(
                id = it.id,
                facultyId = faculty.id,
                abbreviation = it.abbreviation,
                name = it.name,
                lastModifiedTimestamp = it.lastModifiedTimestamp
            )
        }
        return FacultyWithCoursesOfStudiesJunction(faculty, coursesOfStudies)
    }
}