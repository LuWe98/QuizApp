package com.example.quizapp.backdrop

interface BackDropAnimListener {

    fun onProgressChanged(progress: Float) {}

    fun onPostAnimation(expanded: Boolean) {}

}