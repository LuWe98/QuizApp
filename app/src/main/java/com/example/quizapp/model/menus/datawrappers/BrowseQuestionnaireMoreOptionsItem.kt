package com.example.quizapp.model.menus.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.menus.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class BrowseQuestionnaireMoreOptionsItem(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
) : SelectionTypeItemMarker<BrowseQuestionnaireMoreOptionsItem> {

    DOWNLOAD(
        iconRes = R.drawable.ic_download,
        textRes = R.string.download
    ),
    OPEN(
        iconRes = R.drawable.ic_open_in_new,
        textRes = R.string.open
    );

}