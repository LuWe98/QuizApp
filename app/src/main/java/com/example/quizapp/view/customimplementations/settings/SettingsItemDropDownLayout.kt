package com.example.quizapp.view.customimplementations.settings

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.example.quizapp.R
import com.example.quizapp.databinding.SettingsItemDropdownBinding

class SettingsItemDropDownLayout constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    val binding: SettingsItemDropdownBinding = SettingsItemDropdownBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SettingsItemDropDownLayout, defStyleAttr, defStyleRes).let { typedArray ->
            title = typedArray.getString(R.styleable.SettingsItemDropDownLayout_title) ?: ""
            text = typedArray.getString(R.styleable.SettingsItemDropDownLayout_text) ?: ""
            icon = typedArray.getDrawable(R.styleable.SettingsItemDropDownLayout_icon)
            dropDownIcon = typedArray.getDrawable(R.styleable.SettingsItemDropDownLayout_dropDownIcon)
            typedArray.recycle()
        }
    }

    var title = ""
        get() = binding.title.text as String
        set(value) {
            binding.title.text = value
            field = value
        }

    var text = ""
        get() = binding.dropDownText.text as String
        set(value) {
            binding.dropDownText.text = value
            field = value
        }

    var icon: Drawable? = null
        get() = binding.icon.drawable
        set(value) {
            binding.icon.setImageDrawable(value)
            field = value
        }

    var dropDownIcon: Drawable? = null
        get() = binding.dropDownIcon.drawable
        set(value) {
            binding.dropDownIcon.setImageDrawable(value)
            field = value
        }

    fun setTextWithRes(@StringRes textRes: Int){
        text = context.getString(textRes)
    }

}