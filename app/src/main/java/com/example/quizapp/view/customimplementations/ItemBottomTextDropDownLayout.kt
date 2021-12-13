package com.example.quizapp.view.customimplementations

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.databinding.CustomViewItemBottomTextDropdownBinding
import com.example.quizapp.extensions.dp
import com.example.quizapp.extensions.getThemeColor

class ItemBottomTextDropDownLayout constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    val binding = CustomViewItemBottomTextDropdownBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ItemBottomTextDropDownLayout, defStyleAttr, defStyleRes).let { typedArray ->
            title = typedArray.getString(R.styleable.ItemBottomTextDropDownLayout_title) ?: ""
            text = typedArray.getString(R.styleable.ItemBottomTextDropDownLayout_text) ?: ""
            icon = typedArray.getDrawable(R.styleable.ItemBottomTextDropDownLayout_icon)
            dropDownIcon = typedArray.getDrawable(R.styleable.ItemBottomTextDropDownLayout_dropDownIcon)

            typedArray.getDimension(R.styleable.ItemBottomTextDropDownLayout_iconPadding, 10.dp.toFloat()).toInt().let { padding ->
                binding.icon.setPadding(padding, padding, padding, padding)
            }
            typedArray.getColor(R.styleable.ItemBottomTextDropDownLayout_titleColor, getThemeColor(R.attr.defaultTextColor)).let { textColor ->
                binding.title.setTextColor(textColor)
            }
            typedArray.getColor(R.styleable.ItemBottomTextDropDownLayout_textColor, getThemeColor(R.attr.defaultTextColor)).let { textColor ->
                binding.text.setTextColor(textColor)
            }
            typedArray.getDimension(R.styleable.ItemBottomTextDropDownLayout_dropDownIconPadding, 5.dp.toFloat()).toInt().let { padding ->
                binding.dropDownIcon.setPadding(padding, padding, padding, padding)
            }
            typedArray.recycle()
        }

        setBackgroundResource(R.drawable.ripple_round_corners_15_dp)
    }

    var title = ""
        get() = binding.title.text as String
        set(value) {
            binding.title.text = value
            field = value
        }

    var text = ""
        get() = binding.text.text as String
        set(value) {
            binding.text.text = value
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