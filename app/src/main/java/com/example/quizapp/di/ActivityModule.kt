package com.example.quizapp.di

import android.content.Context
import com.example.quizapp.view.QuizActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    fun provideQuizActivity(@ActivityContext context: Context): QuizActivity = if(context is QuizActivity) context else throw IllegalStateException()

}