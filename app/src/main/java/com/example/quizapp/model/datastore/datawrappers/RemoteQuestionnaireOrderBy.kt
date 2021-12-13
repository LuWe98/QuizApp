package com.example.quizapp.model.datastore.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class RemoteQuestionnaireOrderBy(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
): SelectionTypeItemMarker<RemoteQuestionnaireOrderBy> {

    TITLE(
        textRes = R.string.title,
        iconRes = R.drawable.ic_title
    ),
    AUTHOR_NAME(
        textRes = R.string.authorName,
        iconRes = R.drawable.ic_person
    ),
    LAST_UPDATED(
        textRes = R.string.lastUpdated,
        iconRes = R.drawable.ic_update
    )

}