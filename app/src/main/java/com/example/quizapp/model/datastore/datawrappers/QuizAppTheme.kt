package com.example.quizapp.model.datastore.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import com.example.quizapp.R
import com.example.quizapp.model.menus.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class QuizAppTheme(
    val appCompatId: Int,
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
): SelectionTypeItemMarker<QuizAppTheme> {

    SYSTEM_DEFAULT(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        R.string.systemDefault,
        R.drawable.ic_settings
    ),
    DARK(
        AppCompatDelegate.MODE_NIGHT_YES,
        R.string.dark,
        R.drawable.ic_dark_mode_alt
    ),
    LIGHT(
        AppCompatDelegate.MODE_NIGHT_NO,
        R.string.light,
        R.drawable.ic_light_mode
    );

}