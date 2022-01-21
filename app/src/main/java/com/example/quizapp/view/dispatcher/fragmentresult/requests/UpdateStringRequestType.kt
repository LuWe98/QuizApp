package com.example.quizapp.view.dispatcher.fragmentresult.requests

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher

sealed class UpdateStringRequestType constructor(
    @DrawableRes val iconRes: Int,
    @StringRes val hintRes: Int,
    @StringRes val titleRes: Int,
    val resultProvider: (String) -> (FragmentResultDispatcher.UpdateStringValueResult)
): Parcelable {

    abstract val currentStringValue: String

}