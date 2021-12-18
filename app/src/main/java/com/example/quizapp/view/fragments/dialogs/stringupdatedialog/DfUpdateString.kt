package com.example.quizapp.view.fragments.dialogs.stringupdatedialog

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.DfUpdateStringValueBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmUpdateStringValueDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfUpdateString : BindingDialogFragment<DfUpdateStringValueBinding>() {

    private val vmUpdate : VmUpdateStringValueDialog by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews() {
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        binding.apply {
            editText.setText(vmUpdate.updatedText)

            vmUpdate.requestType.apply {
                editText.hint = getString(hintRes)
                editText.setCompoundDrawablesRelative(getDrawable(iconRes), null, null, null)
                editText.setDrawableSize(50.dp)
                tvTitle.setText(titleRes)
            }

            showKeyboard(editText)
            editText.requestFocus()
        }
    }

    private fun initListeners(){
        binding.apply {
            editText.onTextChanged(vmUpdate::onEditTextChanged)
            btnConfirm.onClick(vmUpdate::onConfirmButtonClicked)
            btnCancel.onClick(vmUpdate::onCancelButtonClicked)
        }
    }
}