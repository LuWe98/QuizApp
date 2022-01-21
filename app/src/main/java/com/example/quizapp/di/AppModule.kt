package com.example.quizapp.di

import android.content.Context
import androidx.room.Room
import com.example.quizapp.QuizApplication
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.apiclasses.*
import com.example.quizapp.model.ktor.client.KtorClientAuth
import com.example.quizapp.model.ktor.client.KtorClientExceptionHandler
import com.example.quizapp.model.databases.room.LocalDatabase
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.ktor.backendsyncer.BackendSyncer
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
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideQuizApplication(@ApplicationContext context: Context) = context as QuizApplication

    @Provides
    @Singleton
    fun provideDataMapper() = DataMapper()

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ) = PreferencesRepository(context)


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
        roomDatabase: LocalDatabase
    ) = LocalRepository(
        roomDatabase,
        roomDatabase.getQuestionaryDao(),
        roomDatabase.getQuestionDao(),
        roomDatabase.getAnswerDao(),
        roomDatabase.getFacultyDao(),
        roomDatabase.getCourseOfStudiesDao(),
        roomDatabase.getQuestionnaireCourseOfStudiesRelationDao(),
        roomDatabase.getFacultyCourseOfStudiesRelationDao(),
        roomDatabase.getLocallyDeletedQuestionnaireDao(),
        roomDatabase.getLocallyAnsweredQuestionnairesDao()
    )

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())


    @Singleton
    @Provides
    fun provideKtorClientAuth(
        applicationScope: CoroutineScope,
        preferencesRepository: PreferencesRepository,
        ktorClientProvider: Provider<HttpClient>,
        userApiProvider: Provider<UserApi>
    ) = KtorClientAuth(
        applicationScope,
        preferencesRepository,
        ktorClientProvider,
        userApiProvider
    )


    @Singleton
    @Provides
    fun provideBackendExceptionHandler(preferencesRepository: PreferencesRepository) = KtorClientExceptionHandler(preferencesRepository)


    @Provides
    @Singleton
    fun provideKtorClient(
        ktorClientAuth: KtorClientAuth,
        ktorClientExceptionHandler: KtorClientExceptionHandler,
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

        install(Auth) {
            ktorClientAuth.registerJwtAuth(this)
        }

        engine {
            connectTimeout = 20_000
            socketTimeout = 20_000
        }

        HttpResponseValidator {
            validateResponse { ktorClientExceptionHandler.validateResponse(it) }
            handleResponseException { ktorClientExceptionHandler.handleException(it) }
        }
    }

    @Provides
    @Singleton
    fun provideUserApi(ktorClient: HttpClient) = UserApi(ktorClient)

    @Provides
    @Singleton
    fun provideQuestionnaireApi(ktorClient: HttpClient) = QuestionnaireApi(ktorClient)

    @Provides
    @Singleton
    fun provideFilledQuestionnaireApi(ktorClient: HttpClient) = FilledQuestionnaireApi(ktorClient)

    @Provides
    @Singleton
    fun provideFacultyApi(ktorClient: HttpClient) = FacultyApi(ktorClient)

    @Provides
    @Singleton
    fun provideCourseOfStudiesApi(ktorClient: HttpClient) = CourseOfStudiesApi(ktorClient)


    @Provides
    @Singleton
    fun provideBackendRepository(
        userApi: UserApi,
        questionnaireApi: QuestionnaireApi,
        filledQuestionnaireApi: FilledQuestionnaireApi,
        facultyApi: FacultyApi,
        courseOfStudiesApi: CourseOfStudiesApi
    ) = BackendRepository(
        userApi,
        questionnaireApi,
        filledQuestionnaireApi,
        facultyApi,
        courseOfStudiesApi
    )


    @Provides
    @Singleton
    fun provideConnectivityHelper(@ApplicationContext context: Context) = ConnectivityHelper(context)


    @Provides
    @Singleton
    fun provideSyncHelper(
        preferencesRepository: PreferencesRepository,
        localRepository: LocalRepository,
        backendRepository: BackendRepository,
        dataMapper: DataMapper
    ) = BackendSyncer(
        preferencesRepository,
        localRepository,
        backendRepository,
        dataMapper
    )

}