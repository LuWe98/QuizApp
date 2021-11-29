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
import com.example.quizapp.model.databases.room.entities.relations.QuestionnaireCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties

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

        return CompleteQuestionnaire(questionnaire, questionsWithAnswers, emptyList())
    }


    fun mapRoomQuestionnaireToMongoQuestionnaire(
        completeQuestionnaire: CompleteQuestionnaire
    ): MongoQuestionnaire {
        return MongoQuestionnaire(
            id = completeQuestionnaire.questionnaire.id,
            title = completeQuestionnaire.questionnaire.title,
            authorInfo = completeQuestionnaire.questionnaire.authorInfo,
            facultyIds = completeQuestionnaire.allFaculties.map(Faculty::id),
            courseOfStudiesIds = completeQuestionnaire.allCoursesOfStudies.map(CourseOfStudies::id),
            subject = completeQuestionnaire.questionnaire.subject,
            questionnaireVisibility = completeQuestionnaire.questionnaire.visibility,
            lastModifiedTimestamp = completeQuestionnaire.questionnaire.lastModifiedTimestamp
        ).apply {
            questions = completeQuestionnaire.questionsWithAnswers.map { qwa ->
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

    fun mapMongoQuestionnaireToRoomQuestionnaireCourseOfStudiesRelation(mongoQuestionnaire: MongoQuestionnaire) =
        mongoQuestionnaire.courseOfStudiesIds.distinct().map { courseOfStudiesId ->
            QuestionnaireCourseOfStudiesRelation(mongoQuestionnaire.id, courseOfStudiesId)
        }

    fun mapRoomQuestionnaireToMongoFilledQuestionnaire(
        completeCompleteQuestionnaire: CompleteQuestionnaire
    ) = MongoFilledQuestionnaire(
        questionnaireId = completeCompleteQuestionnaire.questionnaire.id,
        userId = completeCompleteQuestionnaire.questionnaire.authorInfo.userId
    ).apply {
        questions = completeCompleteQuestionnaire.questionsWithAnswers.filter(QuestionWithAnswers::isAnswered).map { qwa ->
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
    ) = mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(completeCompleteQuestionnaire.questionnaire)

    fun mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(
        questionnaire: Questionnaire
    ) = MongoFilledQuestionnaire(
        questionnaireId = questionnaire.id,
        userId = questionnaire.authorInfo.userId
    )


    fun mapMongoFacultyToRoomFaculty(mongoFaculty: MongoFaculty) : Faculty = Faculty(
        id = mongoFaculty.id,
        abbreviation = mongoFaculty.abbreviation,
        name = mongoFaculty.name,
        lastModifiedTimestamp = mongoFaculty.lastModifiedTimestamp
    )

    fun mapRoomFacultyToMongoFaculty(roomFaculty: Faculty): MongoFaculty = MongoFaculty(
        id = roomFaculty.id,
        abbreviation = roomFaculty.abbreviation,
        name = roomFaculty.name,
        lastModifiedTimestamp = roomFaculty.lastModifiedTimestamp
    )

    fun mapMongoCourseOfStudiesToRoomCourseOfStudies(mongoCourseOfStudies: MongoCourseOfStudies): Pair<CourseOfStudies, List<FacultyCourseOfStudiesRelation>> {
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

    fun mapRoomCourseOfStudiesToMongoCourseOfStudies(courseOfStudies: CourseOfStudies, facultyIds: List<String>) = MongoCourseOfStudies(
        id = courseOfStudies.id,
        facultyIds = facultyIds,
        abbreviation = courseOfStudies.abbreviation,
        name = courseOfStudies.name,
        degree = courseOfStudies.degree,
        lastModifiedTimestamp = courseOfStudies.lastModifiedTimestamp
    )
}