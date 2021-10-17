package com.example.quizapp.model.room

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quizapp.model.room.dao.*
import com.example.quizapp.model.room.dao.sync.LocallyAnsweredQuestionnaireDao
import com.example.quizapp.model.room.dao.sync.LocallyDeletedFilledQuestionnaireDao
import com.example.quizapp.model.room.dao.sync.LocallyDeletedQuestionnaireDao
import com.example.quizapp.model.room.dao.sync.LocallyDownloadedQuestionnaireDao
import com.example.quizapp.model.room.entities.*
import com.example.quizapp.model.room.entities.sync.LocallyAnsweredQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDeletedFilledQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDownloadedQuestionnaire
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
        LocallyDeletedFilledQuestionnaire::class,
        LocallyAnsweredQuestionnaire::class,
        LocallyDownloadedQuestionnaire::class
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
    abstract fun getDownloadedQuestionnaireDao(): LocallyDownloadedQuestionnaireDao
    abstract fun getLocallyDeletedQuestionnaireDao(): LocallyDeletedQuestionnaireDao
    abstract fun getLocallyDeletedFilledQuestionnaireDao(): LocallyDeletedFilledQuestionnaireDao
    abstract fun getLocallyAnsweredQuestionnairesDao(): LocallyAnsweredQuestionnaireDao

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