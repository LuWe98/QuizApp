package com.example.quizapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.quizapp.R
import com.example.quizapp.model.datastore.EncryptionUtil
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.OkHttpAuthInterceptor
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.apiclasses.TodoCalls
import com.example.quizapp.model.ktor.apiclasses.UserApi
import com.example.quizapp.model.room.LocalDatabase
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.utils.ConnectivityHelper
import com.example.quizapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATASTORE_NAME)

    @Provides
    @Singleton
    fun provideEncryptionUtil(
        @ApplicationContext context: Context
    ) = EncryptionUtil(
        context.getString(R.string.datastoreEncryptionSecretKey),
        context.getString(R.string.datastoreEncryptionSalt),
        context.getString(R.string.datastoreEncryptionIv)
    )

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context,
        encryptionUtil: EncryptionUtil
    ) = PreferencesRepository(
        context.dataStore,
        encryptionUtil
    )

    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext context: Context,
        callback: LocalDatabase.Callback
    ) =
        Room.databaseBuilder(context, LocalDatabase::class.java, Constants.ROOM_DATABASE_NAME)
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
    fun provideOkHttpAuthInterceptor() = OkHttpAuthInterceptor()

    @Provides
    @Singleton
    fun provideKtorClient(authInterceptor: OkHttpAuthInterceptor): HttpClient = HttpClient(OkHttp) {
        install(DefaultRequest) {
            url.takeFrom(URLBuilder().takeFrom(Constants.BACKEND_URL).apply {
                encodedPath += url.encodedPath
                //TODO -> Change later to HTTPS
                protocol = URLProtocol.HTTP
            })

            contentType(ContentType.Application.Json)
        }

        install(JsonFeature) {
            serializer = GsonSerializer {
                setPrettyPrinting()
                disableHtmlEscaping()
            }
        }

        engine {
            config {
                connectTimeout(60, TimeUnit.SECONDS)
            }
            addInterceptor(authInterceptor)
        }
    }


    @Provides
    @Singleton
    fun provideBackendRepository(ktorClient: HttpClient) = BackendRepository(
        ktorClient,
        TodoCalls(ktorClient),
        UserApi(ktorClient)
    )



    @Provides
    @Singleton
    fun provideConnectivityHelper(@ApplicationContext context: Context) = ConnectivityHelper(context)



//    Ktor Different Settings
//    install(JsonFeature) {
//        Kotlin Serialization
//                serializer = KotlinxSerializer(
//            kotlinx.serialization.json.Json
//            {
//                prettyPrint = true
//                isLenient = true
//                ignoreUnknownKeys = true
//            }
//    }
//
//    install(ResponseObserver)
//    {
//        onResponse { response ->
//
//        }
//    }
//
//    engine {
//        connectTimeout = 100_000
//        socketTimeout = 100_000
//    }
//    connectTimeout = 100_000
//    socketTimeout = 100_000
}