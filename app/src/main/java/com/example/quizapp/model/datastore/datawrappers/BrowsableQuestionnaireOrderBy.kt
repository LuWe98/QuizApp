package com.example.quizapp.model.datastore.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class BrowsableQuestionnaireOrderBy(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
): SelectionTypeItemMarker<BrowsableQuestionnaireOrderBy> {

    TITLE(
        textRes = R.string.title,
        iconRes = R.drawable.ic_title
    ),
    LAST_UPDATED(
        textRes = R.string.lastUpdated,
        iconRes = R.drawable.ic_update
    )

}