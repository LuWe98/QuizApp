package com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class BrowseQuestionnaireMoreOptionsItem(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
) : SelectionTypeItemMarker<BrowseQuestionnaireMoreOptionsItem> {

    OPEN(
        iconRes = R.drawable.ic_open_in_new,
        textRes = R.string.open
    ),
    DOWNLOAD(
        iconRes = R.drawable.ic_download,
        textRes = R.string.download
    ),
    DELETE(
        iconRes = R.drawable.ic_delete,
        textRes = R.string.delete
    );

}