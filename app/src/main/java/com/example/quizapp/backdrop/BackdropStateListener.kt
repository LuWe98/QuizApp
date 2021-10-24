package com.example.quizapp.backdrop

interface BackdropStateListener {

    fun onPreToggle(expanded: Boolean) {}

    fun onToggle(expanded: Boolean) {}

    fun onPostAnimation(expanded: Boolean) {}

}