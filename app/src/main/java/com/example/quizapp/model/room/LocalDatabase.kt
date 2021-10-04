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
        GivenAnswer::class,
        Question::class,
        Questionnaire::class,
        Role::class,
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
    abstract fun getGivenAnswersDao(): GivenAnswerDao
    abstract fun getUserRoleDao(): RoleDao
    abstract fun getFacultyDao(): FacultyDao
    abstract fun getCourseOfStudiesDao(): CourseOfStudiesDao
    abstract fun getSubjectDao(): SubjectDao

    class Callback @Inject constructor(
        private val repoProvider: Provider<LocalRepository>,
        @ApplicationContext val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            repoProvider.get().let { repo ->
                scope.launch(Dispatchers.IO) {
                    RandomQuestionnaireCreatorUtil.generateAndInsertRandomData(
                        questionnaireAmount = 100,
                        minQuestionsPerQuestionnaire = 20,
                        maxQuestionsPerQuestionnaire = 30,
                        minAnswersPerQuestion = 2,
                        maxAnswersPerQuestion = 5
                    ).let { (questionnaires, questions, answers) ->
                        repo.insert(questionnaires)
                        repo.insert(questions)
                        repo.insert(answers)
                    }
                }
            }
        }
    }
}