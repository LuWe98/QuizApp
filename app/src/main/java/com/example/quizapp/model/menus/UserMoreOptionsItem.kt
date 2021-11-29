package com.example.quizapp.model.menus

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class UserMoreOptionsItem(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
): SelectionTypeItemMarker<UserMoreOptionsItem> {

    CHANGE_ROLE(
        R.string.changeUserRole,
        R.drawable.ic_role_badge
    ),
    VIEW_CREATED_QUESTIONNAIRES(
        R.string.browseCreatedQuestionnaires,
        R.drawable.ic_question
    ),
    DELETE(
        R.string.delete,
        R.drawable.ic_delete
    );

}