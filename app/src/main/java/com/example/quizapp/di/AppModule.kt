package com.example.quizapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.apiclasses.*
import com.example.quizapp.model.ktor.authentification.BasicAuthCredentialsProvider
import com.example.quizapp.model.ktor.exceptions.BackendExceptionHandler
import com.example.quizapp.model.room.LocalDatabase
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.utils.BackendSyncer
import com.example.quizapp.utils.ConnectivityHelper
import com.example.quizapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATASTORE_NAME)

    @Provides
    @Singleton
    fun providePreferencesManager(
        applicationScope: CoroutineScope,
        @ApplicationContext context: Context
    ) = PreferencesRepository(
        applicationScope,
        context.dataStore
    )

    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext context: Context,
        callback: LocalDatabase.Callback
    ) = Room.databaseBuilder(context, LocalDatabase::class.java, Constants.ROOM_DATABASE_NAME)
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    @Singleton
    fun provideRoomRepository(
        applicationScope: CoroutineScope,
        roomDatabase: LocalDatabase
    ) = LocalRepository(
        applicationScope,
        roomDatabase,
        roomDatabase.getQuestionaryDao(),
        roomDatabase.getQuestionDao(),
        roomDatabase.getAnswerDao(),
        roomDatabase.getFacultyDao(),
        roomDatabase.getCourseOfStudiesDao(),
        roomDatabase.getSubjectDao(),
        roomDatabase.getLocallyDeletedQuestionnaireDao(),
        roomDatabase.getLocallyDeletedFilledQuestionnaireDao(),
        roomDatabase.getLocallyAnsweredQuestionnairesDao(),
        roomDatabase.getLocallyDeletedUsersDao()
    )

    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())


    @Singleton
    @Provides
    fun provideBasicAuthHelper(preferencesRepository: PreferencesRepository): BasicAuthCredentialsProvider = BasicAuthCredentialsProvider(preferencesRepository)


    @Singleton
    @Provides
    fun provideBackendExceptionHandler(preferencesRepository: PreferencesRepository, localRepository: LocalRepository)
        = BackendExceptionHandler(localRepository, preferencesRepository)


    @Provides
    @Singleton
    fun provideKtorClient(
        basicAuth: BasicAuthCredentialsProvider,
        backendExceptionHandler: BackendExceptionHandler,
    ): HttpClient = HttpClient(Android) {
        install(DefaultRequest) {
            expectSuccess = false

            //TODO -> Change later to HTTPS
            url.takeFrom(URLBuilder().takeFrom(Constants.BACKEND_PATH).apply {
                encodedPath += url.encodedPath
                protocol = URLProtocol.HTTP
            })

            contentType(ContentType.Application.Json)
        }

        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = false
            })
        }

        install(Auth, basicAuth::registerBasicAuth)

        engine {
            connectTimeout = 100_000
            socketTimeout = 100_000
        }

        HttpResponseValidator {
            validateResponse { backendExceptionHandler.validateResponse(it) }
            handleResponseException { backendExceptionHandler.handleException(it) }
        }

//        MessageDigest.getInstance("")
    }


    @Provides
    @Singleton
    fun provideBackendRepository(ktorClient: HttpClient) = BackendRepository(
        ktorClient,
        AdminApi(ktorClient),
        UserApi(ktorClient),
        QuestionnaireApi(ktorClient),
        FilledQuestionnaireApi(ktorClient),
        FacultyApi(ktorClient),
        CourseOfStudiesApi(ktorClient),
        SubjectApi(ktorClient)
    )


    @Provides
    @Singleton
    fun provideConnectivityHelper(@ApplicationContext context: Context) = ConnectivityHelper(context)


    @Provides
    @Singleton
    fun provideSyncHelper(
        applicationScope: CoroutineScope,
        localRepository: LocalRepository,
        backendRepository: BackendRepository,
        preferencesRepository: PreferencesRepository
    ) = BackendSyncer(applicationScope, localRepository, backendRepository, preferencesRepository)

}