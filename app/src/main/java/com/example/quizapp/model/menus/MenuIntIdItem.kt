package com.example.quizapp.model.menus

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.extensions.generateDiffItemCallback

data class MenuIntIdItem(
    val id: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int
){
    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(MenuIntIdItem::id)
    }
}
