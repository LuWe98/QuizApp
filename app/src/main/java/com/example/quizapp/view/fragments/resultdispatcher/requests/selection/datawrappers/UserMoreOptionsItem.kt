package com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionTypeItemMarker
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
        R.drawable.ic_answer
    ),
    DELETE(
        R.string.delete,
        R.drawable.ic_delete
    );

}