package com.example.quizapp.view.customimplementations

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.example.quizapp.R
import com.example.quizapp.databinding.CustomViewListAvailabilityBinding

class DataAvailabilityLayout constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    val binding = CustomViewListAvailabilityBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.DataAvailabilityLayout, defStyleAttr, defStyleRes).let { typedArray ->
            icon = typedArray.getDrawable(R.styleable.DataAvailabilityLayout_availabilityIcon)
            title = typedArray.getString(R.styleable.DataAvailabilityLayout_availabilityTitle) ?: ""
            text = typedArray.getString(R.styleable.DataAvailabilityLayout_availabilityText) ?: ""

            typedArray.recycle()
        }
    }

    var icon: Drawable? = null
        get() = binding.ivIcon.drawable
        set(value) {
            binding.ivIcon.setImageDrawable(value)
            field = value
        }

    var title = ""
        get() = binding.tvTitle.text as String
        set(value) {
            binding.tvTitle.text = value
            field = value
        }

    var text = ""
        get() = binding.tvText.text as String
        set(value) {
            binding.tvText.text = value
            field = value
        }

    fun setIconWithRes(@DrawableRes drawableRes: Int) {
        icon = ContextCompat.getDrawable(context, drawableRes)
    }

    fun setTitleWithRes(@StringRes titleRes: Int) {
        title = context.getString(titleRes)
    }

    fun setTextWithRes(@StringRes textRes: Int) {
        text = context.getString(textRes)
    }
}