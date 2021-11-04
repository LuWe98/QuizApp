package com.example.quizapp.model.menus

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.example.quizapp.utils.DiffCallbackUtil

data class MenuItem(
    @IdRes val id: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int
){
    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<MenuItem> { old, new ->  old.id == new.id }
    }
}
