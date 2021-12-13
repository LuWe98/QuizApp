package com.example.quizapp.model.datastore.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class LocalQuestionnaireOrderBy(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
) : SelectionTypeItemMarker<LocalQuestionnaireOrderBy> {

    TITLE(
        textRes = R.string.title,
        iconRes = R.drawable.ic_title
    ),
    PROGRESS(
        textRes = R.string.orderByProgress,
        iconRes = R.drawable.ic_bar_chart
    ),
    AUTHOR_NAME(
        textRes = R.string.authorName,
        iconRes = R.drawable.ic_person
    ),
    QUESTION_COUNT(
        textRes = R.string.orderByQuestionCount,
        iconRes = R.drawable.ic_question_mark
    ),
    LAST_UPDATED(
        textRes = R.string.lastUpdated,
        iconRes = R.drawable.ic_update
    );

}