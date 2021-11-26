package com.example.quizapp.model.menus

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class FacultyMoreOptionsItem(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
) : SelectionTypeItemMarker<FacultyMoreOptionsItem> {

    EDIT(
        textRes = R.string.edit,
        iconRes = R.drawable.ic_edit
    ),
    DELETE(
        textRes = R.string.delete,
        iconRes = R.drawable.ic_delete
    );

}