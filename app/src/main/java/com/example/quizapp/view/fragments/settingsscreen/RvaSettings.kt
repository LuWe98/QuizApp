package com.example.quizapp.view.fragments.settingsscreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import com.example.quizapp.databinding.*
import com.example.quizapp.extensions.getString
import com.example.quizapp.view.recyclerview.impl.BindingViewHolder

class RvaSettings : ListAdapter<SettingsMenuItem, RvaSettings.SettingsItemViewHolder<SettingsMenuItem>>(SettingsMenuItem.DIFF_CALLBACK) {

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1
    private val VIEW_TYPE_TEXT_ITEM = 2
    private val VIEW_TYPE_SWITCH_ITEM = 3
    private val VIEW_TYPE_DROP_DOWN_ITEM = 4

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is SettingsMenuItem.HeaderItem -> VIEW_TYPE_HEADER
        is SettingsMenuItem.ClickableItem -> VIEW_TYPE_ITEM
        is SettingsMenuItem.TextItem -> VIEW_TYPE_TEXT_ITEM
        is SettingsMenuItem.SwitchItem -> VIEW_TYPE_SWITCH_ITEM
        is SettingsMenuItem.DropDownItem -> VIEW_TYPE_DROP_DOWN_ITEM
    }

    @Suppress("unchecked_cast")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsItemViewHolder<SettingsMenuItem> {
        val layoutInflater = LayoutInflater.from(parent.context)
        return (when (viewType) {
            VIEW_TYPE_HEADER -> SettingsItemViewHolder.HeaderViewHolder(SettingsSectionHeaderBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_ITEM -> SettingsItemViewHolder.ItemViewHolder(SettingsItemBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_TEXT_ITEM -> SettingsItemViewHolder.TextItemViewHolder(SettingsItemTextBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_SWITCH_ITEM -> SettingsItemViewHolder.SwitchItemViewHolder(SettingsItemSwitchBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_DROP_DOWN_ITEM -> SettingsItemViewHolder.DropDownItemViewHolder(SettingsItemDropdownBinding.inflate(layoutInflater, parent, false))
            else -> throw IllegalArgumentException()
        } as SettingsItemViewHolder<SettingsMenuItem>)
    }

    override fun onBindViewHolder(holder: SettingsItemViewHolder<SettingsMenuItem>, position: Int) {
        holder.bind(getItem(position))
    }


    sealed class SettingsItemViewHolder<T : SettingsMenuItem>(binding: ViewBinding) : BindingViewHolder<T>(binding) {

        class HeaderViewHolder(private val binding: SettingsSectionHeaderBinding) : SettingsItemViewHolder<SettingsMenuItem.HeaderItem>(binding) {
            override fun bind(item: SettingsMenuItem.HeaderItem) {
                binding.title.text = binding.getString(item.titleRes)
            }
        }

        class ItemViewHolder(binding: SettingsItemBinding) : SettingsItemViewHolder<SettingsMenuItem.ClickableItem>(binding) {
            override fun bind(item: SettingsMenuItem.ClickableItem) {

            }
        }

        class TextItemViewHolder(binding: SettingsItemTextBinding) : SettingsItemViewHolder<SettingsMenuItem.TextItem>(binding) {
            override fun bind(item: SettingsMenuItem.TextItem) {

            }
        }

        class SwitchItemViewHolder(binding: SettingsItemSwitchBinding) : SettingsItemViewHolder<SettingsMenuItem.SwitchItem>(binding) {
            override fun bind(item: SettingsMenuItem.SwitchItem) {

            }
        }

        class DropDownItemViewHolder(binding: SettingsItemDropdownBinding) : SettingsItemViewHolder<SettingsMenuItem.DropDownItem>(binding) {
            override fun bind(item: SettingsMenuItem.DropDownItem) {

            }
        }
    }
}