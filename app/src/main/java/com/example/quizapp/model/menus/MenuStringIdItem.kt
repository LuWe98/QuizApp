package com.example.quizapp.model.menus

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.utils.DiffCallbackUtil

data class MenuStringIdItem(
    val id: String,
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int
){
    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<MenuStringIdItem> { old, new ->  old.id == new.id }
    }
}
