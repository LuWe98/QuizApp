package com.example.quizapp.model.databases.room

import com.example.quizapp.model.databases.room.dao.*
import com.example.quizapp.model.databases.room.entities.*
import kotlin.reflect.KClass

interface LocalRepositoryI {

    val questionnaireDao: QuestionnaireDao
    val questionDao: QuestionDao
    val answerDao: AnswerDao
    val facultyDao: FacultyDao
    val courseOfStudiesDao: CourseOfStudiesDao
    val questionnaireCourseOfStudiesRelationDao: QuestionnaireCourseOfStudiesRelationDao
    val facultyCourseOfStudiesRelationDao: FacultyCourseOfStudiesRelationDao
    val locallyDeletedQuestionnaireDao: LocallyDeletedQuestionnaireDao
    val locallyFilledQuestionnaireToUploadDao: LocallyFilledQuestionnaireToUploadDao


    /**
     * This method returns the DAO object for the given Entity Class
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : EntityMarker> getBaseDaoWith(entity: KClass<T>) : BaseDao<T> = when (entity) {
        Answer::class -> answerDao
        Question::class -> questionDao
        Questionnaire::class -> questionnaireDao
        Faculty::class -> facultyDao
        CourseOfStudies::class -> courseOfStudiesDao
        QuestionnaireCourseOfStudiesRelation::class -> questionnaireCourseOfStudiesRelationDao
        FacultyCourseOfStudiesRelation::class -> facultyCourseOfStudiesRelationDao
        LocallyDeletedQuestionnaire::class -> locallyDeletedQuestionnaireDao
        LocallyFilledQuestionnaireToUpload::class -> locallyFilledQuestionnaireToUploadDao
        else -> throw IllegalArgumentException("Entity DAO for entity class '${entity.simpleName}' could not be found! Did you add it to the 'getBaseDaoWith' Method?")
    } as BaseDao<T>
}