package com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class AddEditQuestionMoreOptionsItem(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
) : SelectionTypeItemMarker<AddEditQuestionMoreOptionsItem> {
    EDIT(
        iconRes = R.drawable.ic_edit,
        textRes = R.string.edit
    ),
    DELETE(
        iconRes = R.drawable.ic_delete,
        textRes = R.string.delete
    );
}