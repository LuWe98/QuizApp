package com.example.quizapp.model.menus

import android.os.Parcelable
import com.example.quizapp.utils.DiffCallbackUtil

interface SelectionTypeItemMarker <T : Enum<T>> : Parcelable {

    val textRes: Int
    val iconRes: Int

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<SelectionTypeItemMarker<*>> { old, new -> old == new }
    }
}