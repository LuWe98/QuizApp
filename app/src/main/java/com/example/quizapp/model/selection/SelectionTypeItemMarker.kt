package com.example.quizapp.model.selection

import android.os.Parcelable
import com.example.quizapp.extensions.generateDiffItemCallback

interface SelectionTypeItemMarker <T : Enum<T>> : Parcelable {

    val textRes: Int
    val iconRes: Int

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(SelectionTypeItemMarker<*>::textRes)
    }
}