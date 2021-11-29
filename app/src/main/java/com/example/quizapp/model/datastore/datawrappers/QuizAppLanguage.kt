package com.example.quizapp.model.datastore.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.menus.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
enum class QuizAppLanguage(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int = R.drawable.ic_language
) : SelectionTypeItemMarker<QuizAppLanguage> {

    ENGLISH(
        R.string.english
    ),
    GERMAN(
        R.string.german
    );

    val locale: Locale
        get() = when (this) {
            ENGLISH -> Locale.ENGLISH
            GERMAN -> Locale.GERMAN
        }

}