package com.example.quizapp.view.dispatcher

interface Dispatcher <T : DispatchEvent> {
    suspend fun dispatch(event: T)
}