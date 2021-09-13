package com.example.quizapp.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.quizapp.R
import com.example.quizapp.ui.ActivityMain
import com.example.quizapp.ui.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import java.lang.ref.WeakReference

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    fun provideNavHostFragment(@ActivityContext context: Context) =
        (context as AppCompatActivity).supportFragmentManager.findFragmentById(if(context is ActivityMain) R.id.mainActivityNavHost else -1) as NavHostFragment

    @Provides
    fun provideNavController(navHostFragment: NavHostFragment) = navHostFragment.navController

    @Provides
    fun provideNavigator(navHostFragment: NavHostFragment) = Navigator(WeakReference(navHostFragment))
}