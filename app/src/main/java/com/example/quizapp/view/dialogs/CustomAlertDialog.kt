package com.example.quizapp.view.dialogs

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import com.example.quizapp.databinding.DialogCustomAlertBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.view.bindingsuperclasses.BindingDialog

class CustomAlertDialog(context: Context): BindingDialog<DialogCustomAlertBinding>(context) {

    var onConfirmButtonClicked: (() -> (Unit))? = null
    var onCancelButtonClicked: (() -> (Unit))? = null

    var title = ""
        get() = binding.tvTitle.text.toString()
        set(value) {
            binding.tvTitle.text = value
            field = value
        }

    var text = ""
        get() = binding.tvText.text.toString()
        set(value) {
            binding.tvText.text = value
            field = value
        }

    var confirmButtonText = ""
        get() = binding.btnConfirm.text.toString()
        set(value) {
            binding.btnConfirm.text = value
            field = value
        }

    var cancelButtonText = ""
        get() = binding.btnCancel.text.toString()
        set(value) {
            binding.btnCancel.text = value
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            btnCancel.onClick {
                onCancelButtonClicked?.invoke()
                dismiss()
            }

            btnConfirm.onClick {
                onConfirmButtonClicked?.invoke()
                dismiss()
            }
        }
    }

    override fun setTitle(@StringRes titleRes: Int) {
        title = context.getString(titleRes)
    }

    fun setText(@StringRes textRes: Int) {
        text = context.getString(textRes)
    }

    fun setConfirmButtonText(@StringRes confirmButtonTextRes: Int) {
        confirmButtonText = context.getString(confirmButtonTextRes)
    }

    fun setCancelButtonText(@StringRes cancelButtonTextRes: Int) {
        cancelButtonText = context.getString(cancelButtonTextRes)
    }
}