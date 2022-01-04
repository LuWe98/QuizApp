package com.example.quizapp.view.dispatcher

import com.example.quizapp.view.QuizActivity

interface DispatchEvent {

    suspend fun execute(quizActivity: QuizActivity)

}