package com.example.quizapp.di

import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher
import com.example.quizapp.view.NavigationDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityRetainedModule {

    @Provides
    @ActivityRetainedScoped
    fun provideNavigationDispatcher() = NavigationDispatcher()

    @Provides
    @ActivityRetainedScoped
    fun provideFragmentResultDispatcher() = FragmentResultDispatcher()

}