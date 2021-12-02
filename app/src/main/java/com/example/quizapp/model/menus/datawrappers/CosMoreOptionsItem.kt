package com.example.quizapp.model.menus.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.menus.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class CosMoreOptionsItem(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
) : SelectionTypeItemMarker<CosMoreOptionsItem> {

    EDIT(
        textRes = R.string.edit,
        iconRes = R.drawable.ic_edit
    ),
    DELETE(
        textRes = R.string.delete,
        iconRes = R.drawable.ic_delete
    );

}