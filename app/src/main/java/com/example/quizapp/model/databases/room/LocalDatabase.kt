package com.example.quizapp.model.databases.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quizapp.model.databases.room.dao.*
import com.example.quizapp.model.databases.room.entities.*
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.typeconverter.RoomTypeConverters
import com.example.quizapp.utils.Constants
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        Questionnaire::class,
        Question::class,
        Answer::class,
        Faculty::class,
        CourseOfStudies::class,
        QuestionnaireCourseOfStudiesRelation::class,
        FacultyCourseOfStudiesRelation::class,
        LocallyDeletedQuestionnaire::class,
        LocallyFilledQuestionnaireToUpload::class
    ],
    version = Constants.ROOM_DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun getQuestionaryDao(): QuestionnaireDao
    abstract fun getQuestionDao(): QuestionDao
    abstract fun getAnswerDao(): AnswerDao
    abstract fun getFacultyDao(): FacultyDao
    abstract fun getCourseOfStudiesDao(): CourseOfStudiesDao
    abstract fun getQuestionnaireCourseOfStudiesRelationDao(): QuestionnaireCourseOfStudiesRelationDao
    abstract fun getFacultyCourseOfStudiesRelationDao(): FacultyCourseOfStudiesRelationDao
    abstract fun getLocallyDeletedQuestionnaireDao(): LocallyDeletedQuestionnaireDao
    abstract fun getLocallyAnsweredQuestionnairesDao(): LocallyFilledQuestionnaireToUploadDao

    companion object {
        const val PLACEHOLDER = "?"
        const val PLACEHOLDER_SEPARATOR = ","
    }
}