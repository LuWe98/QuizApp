package com.example.quizapp.view.customimplementations.backdrop

interface BackdropStateListener {

    fun onPreToggle(expanded: Boolean) {}

    fun onToggle(expanded: Boolean) {}

    fun onPostAnimation(expanded: Boolean) {}

}