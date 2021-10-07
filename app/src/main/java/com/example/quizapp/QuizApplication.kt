package com.example.quizapp

import android.app.Application
import com.example.quizapp.extensions.log
import com.example.quizapp.model.room.LocalRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class QuizApplication : Application()