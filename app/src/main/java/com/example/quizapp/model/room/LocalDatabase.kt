package com.example.quizapp.model.room

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quizapp.model.room.dao.*
import com.example.quizapp.model.room.entities.*
import com.example.quizapp.utils.RandomQuestionnaireCreatorUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        Answer::class,
        Question::class,
        Questionnaire::class,
        User::class,
        UserRole::class,
        Faculty::class,
        CourseOfStudies::class,
        Subject::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun getQuestionaryDao(): QuestionnaireDao
    abstract fun getQuestionDao(): QuestionDao
    abstract fun getAnswerDao(): AnswerDao
    abstract fun getUserDao() : UserDao
    abstract fun getUserRoleDao() : UserRoleDao
    abstract fun getFacultyDao() : FacultyDao
    abstract fun getCourseOfStudiesDao() : CourseOfStudiesDao
    abstract fun getSubjectDao() : SubjectDao

    class Callback @Inject constructor(
        private val repoProvider: Provider<LocalRepository>,
        @ApplicationContext val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            repoProvider.get().let { repo ->
                scope.launch(Dispatchers.IO) {
                    RandomQuestionnaireCreatorUtil.generateAndInsertRandomData(repo, 100, 15, 5)
                }
            }
        }
    }
}