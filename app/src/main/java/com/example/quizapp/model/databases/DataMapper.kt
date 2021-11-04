package com.example.quizapp.model.databases

import com.example.quizapp.model.databases.mongodb.documents.faculty.MongoCourseOfStudies
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
import com.example.quizapp.model.databases.room.entities.relations.FacultyCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire

/**
 * DataMapper for ROOM and MONGO Entities/Documents
 */
object DataMapper {

    fun mapMongoQuestionnaireToRoomCompleteQuestionnaire(
        mongoQuestionnaire: MongoQuestionnaire,
        mongoFilledQuestionnaire: MongoFilledQuestionnaire? = null
    ): CompleteQuestionnaire {
        val questionnaire = Questionnaire(
            id = mongoQuestionnaire.id,
            title = mongoQuestionnaire.title,
            authorInfo = mongoQuestionnaire.authorInfo,
            facultyId = mongoQuestionnaire.facultyId,
            courseOfStudiesId = mongoQuestionnaire.courseOfStudiesId,
            subject = mongoQuestionnaire.subject,
            syncStatus = SyncStatus.SYNCED,
            visibility = mongoQuestionnaire.questionnaireVisibility,
            lastModifiedTimestamp = mongoQuestionnaire.lastModifiedTimestamp
        )

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

        return CompleteQuestionnaire(questionnaire, questionsWithAnswers, null, null)
    }


    fun mapRoomQuestionnaireToMongoQuestionnaire(
        completeCompleteQuestionnaire: CompleteQuestionnaire
    ) = completeCompleteQuestionnaire.run { mapRoomQuestionnaireToMongoQuestionnaire(questionnaire, questionsWithAnswers) }

    fun mapRoomQuestionnaireToMongoQuestionnaire(
        questionnaire: Questionnaire,
        questions: List<Question>, answers: List<Answer>
    ) = mapRoomQuestionnaireToMongoQuestionnaire(questionnaire, questions.map { question ->
        QuestionWithAnswers(question = question, answers = answers.filter { answer -> answer.questionId == question.id })
    })

    fun mapRoomQuestionnaireToMongoQuestionnaire(
        questionnaire: Questionnaire,
        questionsWithAnswers: List<QuestionWithAnswers>
    ): MongoQuestionnaire {
        return MongoQuestionnaire(
            id = questionnaire.id,
            title = questionnaire.title,
            authorInfo = questionnaire.authorInfo,
            facultyId = questionnaire.facultyId,
            courseOfStudiesId = questionnaire.courseOfStudiesId,
            subject = questionnaire.subject,
            questionnaireVisibility = questionnaire.visibility,
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


    fun mapRoomQuestionnaireToMongoFilledQuestionnaire(
        completeCompleteQuestionnaire: CompleteQuestionnaire
    ) = completeCompleteQuestionnaire.run { mapRoomQuestionnaireToMongoFilledQuestionnaire(questionnaire, questionsWithAnswers) }

    fun mapRoomQuestionnaireToMongoFilledQuestionnaire(
        questionnaire: Questionnaire,
        questions: List<Question>,
        answers: List<Answer>
    ) = mapRoomQuestionnaireToMongoFilledQuestionnaire(questionnaire, questions.map { question ->
        QuestionWithAnswers(question = question, answers = answers.filter { answer -> answer.questionId == question.id })
    })

    fun mapRoomQuestionnaireToMongoFilledQuestionnaire(
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

    fun mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(
        completeCompleteQuestionnaire: CompleteQuestionnaire
    ) = completeCompleteQuestionnaire.run { mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(questionnaire) }

    fun mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(
        questionnaire: Questionnaire
    ) = MongoFilledQuestionnaire(
        questionnaireId = questionnaire.id,
        userId = questionnaire.authorInfo.userId
    )


    fun mapMongoFacultyToRoomFaculty(mongoFaculty: MongoFaculty) = Faculty(
        id = mongoFaculty.id,
        abbreviation = mongoFaculty.abbreviation,
        name = mongoFaculty.name,
        lastModifiedTimestamp = mongoFaculty.lastModifiedTimestamp
    )

    fun mapMongoCourseOfStudiesToRoomCourseOfStudies(mongoCourseOfStudies: MongoCourseOfStudies) : Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>> {
        val courseOfStudies = CourseOfStudies(
            id = mongoCourseOfStudies.id,
            abbreviation = mongoCourseOfStudies.abbreviation,
            name = mongoCourseOfStudies.name,
            degree = mongoCourseOfStudies.degree,
            lastModifiedTimestamp = mongoCourseOfStudies.lastModifiedTimestamp
        )

        val facultyCourseOfStudiesRelation = mongoCourseOfStudies.facultyIds.map { facultyId ->
            FacultyCourseOfStudiesRelation(facultyId, courseOfStudies.id)
        }

        return Pair(courseOfStudies, facultyCourseOfStudiesRelation)
    }
}