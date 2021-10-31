package com.example.quizapp.view.fragments.settingsscreen

import android.content.res.Resources
import android.os.Build
import androidx.annotation.StringRes
import com.example.quizapp.R
import java.util.*

enum class QuizAppLanguage(@StringRes val textRes: Int) {
    SYSTEM_DEFAULT(R.string.systemDefault),
    ENGLISH(R.string.english),
    GERMAN(R.string.german);

    val locale: Locale
        get() = run {
        when(this) {
            SYSTEM_DEFAULT -> {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    Resources.getSystem().configuration.locales[0]
                } else {
                    Resources.getSystem().configuration.locale
                }
            }
            ENGLISH -> Locale.ENGLISH
            GERMAN -> Locale.GERMAN
        }
    }

}