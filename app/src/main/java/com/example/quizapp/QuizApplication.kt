package com.example.quizapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.quizapp.model.datastore.PreferenceRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class QuizApplication : Application(){

    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(runBlocking(IO) { preferenceRepository.getTheme().appCompatId })
    }

}