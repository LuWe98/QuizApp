package com.example.quizapp.view.customimplementations

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.databinding.CustomViewTextInputBinding
import com.example.quizapp.extensions.onTextChanged

class CustomTextInput constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    val binding = CustomViewTextInputBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomTextInput, defStyleAttr, defStyleRes).let { typedArray ->
            title = typedArray.getString(R.styleable.CustomTextInput_titleText) ?: ""
            hintText = typedArray.getString(R.styleable.CustomTextInput_hintText) ?: ""
            typedArray.recycle()
        }
    }

    inline fun onTextChanged(crossinline action: (String) -> Unit){
        binding.editText.onTextChanged(action)
    }

    var title
        get() = binding.tvTitle.text as String
        set(value) {
            binding.tvTitle.text = value
        }

    var hintText
        get() = binding.editText.hint.toString()
        set(value) {
            binding.editText.hint = value
        }

    var text
        get() = binding.editText.text!!.toString()
        set(value) {
            binding.editText.setText(value)
        }

    fun setTextWithRes(@StringRes textRes: Int) {
        text = context.getString(textRes)
    }
}