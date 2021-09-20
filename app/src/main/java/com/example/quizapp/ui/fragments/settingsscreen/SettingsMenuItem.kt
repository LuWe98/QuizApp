package com.example.quizapp.ui.fragments.settingsscreen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil

sealed class SettingsMenuItem(open val id: Int) {

    data class HeaderItem(
        override val id: Int,
        @StringRes val titleRes: Int
    ) : SettingsMenuItem(id)

    data class ClickableItem(
        override val id: Int,
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int,
    ) : SettingsMenuItem(id)

    data class TextItem(
        override val id: Int,
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int,
    ) : SettingsMenuItem(id)

    data class SwitchItem(
        override val id: Int,
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int,
    ) : SettingsMenuItem(id)

    data class DropDownItem(
        override val id: Int,
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int,
    ) : SettingsMenuItem(id)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SettingsMenuItem>() {
            override fun areItemsTheSame(oldItem: SettingsMenuItem, newItem: SettingsMenuItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: SettingsMenuItem, newItem: SettingsMenuItem) = oldItem == newItem
        }
    }
}