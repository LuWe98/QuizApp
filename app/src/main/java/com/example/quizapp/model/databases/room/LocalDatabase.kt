package com.example.quizapp.model.databases.room

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quizapp.model.databases.room.dao.*
import com.example.quizapp.model.databases.room.dao.sync.*
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.entities.relations.FacultyCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.entities.relations.QuestionnaireCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.entities.sync.*
import com.example.quizapp.model.databases.room.typeconverter.LocalDatabaseTypeConverter
import com.example.quizapp.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider
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
@TypeConverters(LocalDatabaseTypeConverter::class)
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

    class Callback @Inject constructor(
        private val repoProvider: Provider<LocalRepository>,
        @ApplicationContext val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }

    companion object {
        const val PLACEHOLDER = "?"
        const val PLACEHOLDER_SEPARATOR = ","
    }
}