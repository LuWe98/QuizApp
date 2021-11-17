package com.example.quizapp.view.fragments.dialogs.stringupdatedialog

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.DfUpdateStringValueBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onTextChanged
import com.example.quizapp.extensions.showKeyboard
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmUpdateStringValueDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfUpdateStringValue : BindingDialogFragment<DfUpdateStringValueBinding>() {

    private val vmUpdate : VmUpdateStringValueDialog by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews() {
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        binding.apply {
            vmUpdate.updateType.apply {
                editText.setText(vmUpdate.updatedText)
                inputLayout.hint = getString(hintRes)
                inputLayout.setStartIconDrawable(iconRes)
                tvTitle.setText(titleRes)
            }

            showKeyboard(editText)
            editText.requestFocus()
        }
    }

    private fun initListeners(){
        binding.apply {
            editText.onTextChanged(vmUpdate::onEditTextChanged)
            btnUpdate.onClick {
                setFragmentResult(vmUpdate.updateType.resultKey, Bundle().apply {
                    putString(vmUpdate.updateType.resultKey, vmUpdate.updatedText)
                })
                navigator.popBackStack()
            }
        }
    }
}