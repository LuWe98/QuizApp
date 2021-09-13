package com.example.quizapp.di

import android.content.Context
import androidx.room.Room
import com.example.quizapp.model.datastore.PreferencesManager
import com.example.quizapp.model.externaldatabase.ExternalDatabaseManager
import com.example.quizapp.model.room.LocalDatabase
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        Room.databaseBuilder(context, LocalDatabase::class.java, Constants.DATASTORE_NAME)
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    @Singleton
    fun provideRoomRepository(roomDatabase: LocalDatabase) = LocalRepository(roomDatabase)

    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideExternalDatabaseManager() = ExternalDatabaseManager()
}