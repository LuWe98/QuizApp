package com.example.quizapp.model.datastore.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ManageUsersOrderBy(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
): SelectionTypeItemMarker<ManageUsersOrderBy> {

    USER_NAME(
        R.string.userName,
        R.drawable.ic_person
    ),
    ROLE(
        R.string.role,
        R.drawable.ic_role_badge
    )

}