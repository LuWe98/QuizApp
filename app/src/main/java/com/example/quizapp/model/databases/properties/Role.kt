package com.example.quizapp.model.databases.properties

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Role(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int = R.drawable.ic_role_badge
) : SelectionTypeItemMarker<Role> {

    ADMIN(R.string.roleAdmin),
    CREATOR(R.string.roleCreator),
    USER(R.string.roleUser)

}