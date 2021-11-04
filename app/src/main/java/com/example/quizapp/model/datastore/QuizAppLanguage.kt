package com.example.quizapp.model.datastore

import android.content.res.Resources
import android.os.Build
import androidx.annotation.StringRes
import com.example.quizapp.R
import java.util.*

@Suppress("DEPRECATION")
enum class QuizAppLanguage(@StringRes val textRes: Int) {
    SYSTEM_DEFAULT(R.string.systemDefault),
    ENGLISH(R.string.english),
    GERMAN(R.string.german);

    val locale: Locale
        get() = run {
        when(this) {
            SYSTEM_DEFAULT -> {
                val locale = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    Resources.getSystem().configuration.locales[0]
                } else {
                    Resources.getSystem().configuration.locale
                }
                //TODO -> SCHAUEN WAS PASSIERT WENN MAN KEINE DER SPRACHEN ALS DEFAULT SPRACHE HAT
                // EVENTUEL SYSTEM DEAFAULT ENTFERNEN FÜR DIE SPRACHE
                locale
            }
            ENGLISH -> Locale.ENGLISH
            GERMAN -> Locale.GERMAN
        }
    }
}