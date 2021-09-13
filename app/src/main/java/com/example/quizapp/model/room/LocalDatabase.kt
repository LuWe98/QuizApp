package com.example.quizapp.model.room

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quizapp.model.room.dao.AnswerDao
import com.example.quizapp.model.room.dao.QuestionDao
import com.example.quizapp.model.room.dao.QuestionnaireDao
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire
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
        Questionnaire::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun getQuestionaryDao(): QuestionnaireDao
    abstract fun getQuestionDao(): QuestionDao
    abstract fun getAnswerDao(): AnswerDao


    class Callback @Inject constructor(
        private val repoProvider: Provider<LocalRepository>,
        @ApplicationContext val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            repoProvider.get().apply {
                scope.launch(Dispatchers.IO) {
                    RandomQuestionnaireCreatorUtil.generateData(this@apply, 100, 20, 5)
                }
            }
        }
    }
}