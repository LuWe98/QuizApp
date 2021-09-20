package com.example.quizapp.di

import android.content.Context
import androidx.room.Room
import com.example.quizapp.model.datastore.PreferencesManager
import com.example.quizapp.model.ktor.KtorRepository
import com.example.quizapp.model.ktor.apiclasses.TodoCalls
import com.example.quizapp.model.ktor.apiclasses.UserCalls
import com.example.quizapp.model.room.LocalDatabase
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context) = PreferencesManager(context)

    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context, callback: LocalDatabase.Callback) =
        Room.databaseBuilder(context, LocalDatabase::class.java, Constants.ROOM_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    @Singleton
    fun provideRoomRepository(applicationScope: CoroutineScope, roomDatabase: LocalDatabase) = LocalRepository(
        applicationScope,
        roomDatabase.getQuestionaryDao(),
        roomDatabase.getQuestionDao(),
        roomDatabase.getAnswerDao(),
        roomDatabase.getUserDao(),
        roomDatabase.getUserRoleDao(),
        roomDatabase.getFacultyDao(),
        roomDatabase.getCourseOfStudiesDao(),
        roomDatabase.getSubjectDao()
    )

    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())


    @Provides
    @Singleton
    fun provideKtorClient() = HttpClient(Android) {
        install(DefaultRequest)
        install(JsonFeature) {
            serializer = GsonSerializer {
                setPrettyPrinting()
                disableHtmlEscaping()
            }
        }

        engine {
            connectTimeout = 100_000
            socketTimeout = 100_000
        }
    }

    @Provides
    @Singleton
    fun provideKtorRepository(ktorClient: HttpClient) = KtorRepository(
        TodoCalls(ktorClient),
        UserCalls(ktorClient)
    )
}