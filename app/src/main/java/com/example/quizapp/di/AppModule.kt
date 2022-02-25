package com.example.quizapp.di

import android.content.Context
import androidx.room.Room
import com.example.quizapp.QuizApplication
import com.example.quizapp.extensions.dataStore
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.datastore.PreferenceRepositoryImpl
import com.example.quizapp.model.ktor.BackendRepositoryImpl
import com.example.quizapp.model.ktor.apiclasses.*
import com.example.quizapp.model.ktor.client.KtorClientAuth
import com.example.quizapp.model.ktor.client.KtorClientExceptionHandler
import com.example.quizapp.model.databases.room.LocalDatabase
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.LocalRepositoryImpl
import com.example.quizapp.model.datastore.PreferenceRepository
import com.example.quizapp.model.ktor.BackendRepository
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
    fun providePreferencesRepository(@ApplicationContext context: Context) : PreferenceRepository = PreferenceRepositoryImpl(context.dataStore)


    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext context: Context,
    ) = Room.databaseBuilder(context, LocalDatabase::class.java, Constants.ROOM_DATABASE_NAME)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideRoomRepository(
        roomDatabase: LocalDatabase
    ) : LocalRepository = LocalRepositoryImpl(
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
        preferenceRepository: PreferenceRepository,
        ktorClientProvider: Provider<HttpClient>,
        userApiProvider: Provider<UserApiImpl>
    ) = KtorClientAuth(
        preferenceRepository,
        ktorClientProvider,
        userApiProvider
    )


    @Singleton
    @Provides
    fun provideBackendExceptionHandler(preferenceRepository: PreferenceRepository) = KtorClientExceptionHandler(preferenceRepository)


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
            })
        }

        install(Auth) {
            ktorClientAuth.registerJwtAuth(this)
        }

        engine {
            connectTimeout = 25_000
            socketTimeout = 25_000
        }

        HttpResponseValidator {
            validateResponse(ktorClientExceptionHandler::validateResponse)
            handleResponseException(ktorClientExceptionHandler::handleException)
        }
    }

    @Provides
    @Singleton
    fun provideUserApi(ktorClient: HttpClient) : UserApi = UserApiImpl(ktorClient)

    @Provides
    @Singleton
    fun provideQuestionnaireApi(ktorClient: HttpClient) : QuestionnaireApi = QuestionnaireApiImpl(ktorClient)

    @Provides
    @Singleton
    fun provideFilledQuestionnaireApi(ktorClient: HttpClient) : FilledQuestionnaireApi = FilledQuestionnaireApiImpl(ktorClient)

    @Provides
    @Singleton
    fun provideFacultyApi(ktorClient: HttpClient) : FacultyApi = FacultyApiImpl(ktorClient)

    @Provides
    @Singleton
    fun provideCourseOfStudiesApi(ktorClient: HttpClient) : CourseOfStudiesApi = CourseOfStudiesApiImpl(ktorClient)


    @Provides
    @Singleton
    fun provideBackendRepository(
        userApi: UserApi,
        questionnaireApi: QuestionnaireApi,
        filledQuestionnaireApi: FilledQuestionnaireApi,
        facultyApi: FacultyApi,
        courseOfStudiesApiImpl: CourseOfStudiesApi
    ) : BackendRepository = BackendRepositoryImpl(
        userApi,
        questionnaireApi,
        filledQuestionnaireApi,
        facultyApi,
        courseOfStudiesApiImpl
    )


    @Provides
    @Singleton
    fun provideConnectivityHelper(@ApplicationContext context: Context) = ConnectivityHelper(context)


    @Provides
    @Singleton
    fun provideSyncHelper(
        preferenceRepository: PreferenceRepository,
        localRepository: LocalRepository,
        backendRepository: BackendRepository,
        dataMapper: DataMapper
    ) = BackendSyncer(
        preferenceRepository,
        localRepository,
        backendRepository,
        dataMapper
    )
}