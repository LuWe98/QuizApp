package com.example.quizapp.model.menudatamodels

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.example.quizapp.utils.DiffUtilHelper

data class MenuItem(
    @IdRes val id: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int
){
    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<MenuItem> { old, new ->  old.id == new.id }
    }
}
