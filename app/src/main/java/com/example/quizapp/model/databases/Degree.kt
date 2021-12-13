package com.example.quizapp.model.databases

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Degree(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int = R.drawable.ic_certificate
) : SelectionTypeItemMarker<Degree> {

    BACHELOR(R.string.degreeBachelor),
    MASTER(R.string.degreeMaster);

}