package com.example.quizapp.view.customimplementations.settings

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.quizapp.R
import com.example.quizapp.databinding.CustomViewSettingsItemBinding
import com.example.quizapp.extensions.dp
import com.example.quizapp.extensions.getThemeColor

class SettingsItemActionLayout constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    val binding = CustomViewSettingsItemBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SettingsItemActionLayout, defStyleAttr, defStyleRes).let { typedArray ->
            title = typedArray.getString(R.styleable.SettingsItemActionLayout_title) ?: ""
            icon = typedArray.getDrawable(R.styleable.SettingsItemActionLayout_icon)
            typedArray.getDimension(R.styleable.SettingsItemActionLayout_iconPadding, 10.dp.toFloat()).toInt().let { padding ->
                binding.icon.setPadding(padding, padding, padding, padding)
            }
            typedArray.getColor(R.styleable.SettingsItemActionLayout_titleColor, getThemeColor(R.attr.dominantTextColor)).let { textColor ->
                binding.title.setTextColor(textColor)
            }
            typedArray.recycle()
        }
    }

    var title = ""
        get() = binding.title.text as String
        set(value) {
            binding.title.text = value
            field = value
        }

    var icon: Drawable? = null
        get() = binding.icon.drawable
        set(value) {
            binding.icon.setImageDrawable(value)
            field = value
        }
}