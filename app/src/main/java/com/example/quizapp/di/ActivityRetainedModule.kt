package com.example.quizapp.di

import com.example.quizapp.view.dispatcher.DispatchEventQueueContainer
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher
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
    fun provideDispatcherEventQueue() = DispatchEventQueueContainer()

    @Provides
    @ActivityRetainedScoped
    fun provideNavigationDispatcher(queue: DispatchEventQueueContainer) = NavigationDispatcher(queue)

    @Provides
    @ActivityRetainedScoped
    fun provideFragmentResultDispatcher(queue: DispatchEventQueueContainer) = FragmentResultDispatcher(queue)

}