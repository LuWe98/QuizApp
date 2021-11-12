package com.example.quizapp.model.datastore

import androidx.annotation.StringRes
import com.example.quizapp.R
import java.util.*

enum class QuizAppLanguage(@StringRes val textRes: Int) {
    ENGLISH(R.string.english),
    GERMAN(R.string.german);

    val locale: Locale
        get() = run {
        when(this) {
            ENGLISH -> Locale.ENGLISH
            GERMAN -> Locale.GERMAN
        }
    }
}