package com.example.quizapp.model.databases

import com.example.quizapp.model.databases.mongodb.documents.*
import com.example.quizapp.model.databases.room.entities.*
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.ktor.status.SyncStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataMapper for ROOM and MONGO Entities/Documents
 */
@Singleton
class DataMapper @Inject constructor() {

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
            visibility = mongoQuestionnaire.visibility,
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
    ) = MongoQuestionnaire(
        id = completeQuestionnaire.questionnaire.id,
        title = completeQuestionnaire.questionnaire.title,
        authorInfo = completeQuestionnaire.questionnaire.authorInfo,
        facultyIds = completeQuestionnaire.allFaculties.map(Faculty::id),
        courseOfStudiesIds = completeQuestionnaire.allCoursesOfStudies.map(CourseOfStudies::id),
        subject = completeQuestionnaire.questionnaire.subject,
        visibility = completeQuestionnaire.questionnaire.visibility,
        lastModifiedTimestamp = completeQuestionnaire.questionnaire.lastModifiedTimestamp,
        questionCount = completeQuestionnaire.questionsWithAnswers.size,
        questions = completeQuestionnaire.questionsWithAnswers.map { qwa ->
            qwa.question.let { question ->
                MongoQuestion(
                    id = question.id,
                    questionText = question.questionText,
                    isMultipleChoice = question.isMultipleChoice,
                    questionPosition = question.questionPosition,
                    answers = qwa.answers.map { answer ->
                        MongoAnswer(
                            id = answer.id,
                            answerText = answer.answerText,
                            answerPosition = answer.answerPosition,
                            isAnswerCorrect = answer.isAnswerCorrect
                        )
                    }
                )
            }
        }
    )

    fun mapMongoQuestionnaireToRoomQuestionnaireCourseOfStudiesRelation(mongoQuestionnaire: MongoQuestionnaire) =
        mongoQuestionnaire.courseOfStudiesIds.distinct().map { courseOfStudiesId ->
            QuestionnaireCourseOfStudiesRelation(mongoQuestionnaire.id, courseOfStudiesId)
        }

    fun mapRoomQuestionnaireToMongoFilledQuestionnaire(
        completeCompleteQuestionnaire: CompleteQuestionnaire
    ) = MongoFilledQuestionnaire(
        questionnaireId = completeCompleteQuestionnaire.questionnaire.id,
        userId = completeCompleteQuestionnaire.questionnaire.authorInfo.userId,
        questions = completeCompleteQuestionnaire.questionsWithAnswers.filter(QuestionWithAnswers::isAnswered).map { qwa ->
            MongoFilledQuestion(
                questionId = qwa.question.questionnaireId,
                selectedAnswerIds = qwa.selectedAnswerIds
            )
        }
    )

    fun mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(
        completeCompleteQuestionnaire: CompleteQuestionnaire
    ) = mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(completeCompleteQuestionnaire.questionnaire)

    fun mapRoomQuestionnaireToEmptyMongoFilledMongoEntity(
        questionnaire: Questionnaire
    ) = MongoFilledQuestionnaire(
        questionnaireId = questionnaire.id,
        userId = questionnaire.authorInfo.userId
    )


    fun mapMongoFacultyToRoomFaculty(mongoFaculty: MongoFaculty): Faculty = Faculty(
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