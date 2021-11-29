package com.example.quizapp.model.datastore.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.menus.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class HomeListFilter(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
): SelectionTypeItemMarker<HomeListFilter> {

    ALL_QUESTIONNAIRES(
        textRes = R.string.allQuestionnaires,
        iconRes = R.drawable.ic_edit_list
    ),
    OWN_QUESTIONNAIRES(
        textRes = R.string.allQuestionnaires,
        iconRes = R.drawable.ic_person
    ),
    DOWNLOADED_QUESTIONNAIRES(
        textRes = R.string.allQuestionnaires,
        iconRes = R.drawable.ic_download
    );

}