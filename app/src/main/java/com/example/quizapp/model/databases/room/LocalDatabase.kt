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
import com.example.quizapp.model.databases.room.entities.faculty.Subject
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.entities.sync.*
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
        Subject::class,
        LocallyDeletedQuestionnaire::class,
        LocallyClearedQuestionnaire::class,
        LocallyAnsweredQuestionnaire::class,
        LocallyDeletedUser::class
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
    abstract fun getSubjectDao(): SubjectDao
    abstract fun getLocallyDeletedQuestionnaireDao(): LocallyDeletedQuestionnaireDao
    abstract fun getLocallyDeletedFilledQuestionnaireDao(): LocallyClearedQuestionnaireDao
    abstract fun getLocallyAnsweredQuestionnairesDao(): LocallyAnsweredQuestionnaireDao
    abstract fun getLocallyDeletedUsersDao(): LocallyDeletedUserDao

    class Callback @Inject constructor(
        private val repoProvider: Provider<LocalRepository>,
        @ApplicationContext val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }
}