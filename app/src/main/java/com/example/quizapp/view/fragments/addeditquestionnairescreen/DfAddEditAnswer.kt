package com.example.quizapp.view.fragments.addeditquestionnairescreen

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.DfAddEditAnswerBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onTextChanged
import com.example.quizapp.extensions.showKeyboard
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmAddEditAnswer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfAddEditAnswer: BindingDialogFragment<DfAddEditAnswerBinding>() {

    private val vmAddEdit : VmAddEditAnswer by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        binding.apply {
            tvTitle.setText(vmAddEdit.titleTextRes)

            editText.setText(vmAddEdit.answerText)
            showKeyboard(editText)
            editText.requestFocus()
        }
    }

    private fun initListeners(){
        binding.apply {
            editText.onTextChanged(vmAddEdit::onAnswerTextChanged)
            btnCancel.onClick(vmAddEdit::onCancelButtonClicked)
            btnConfirm.onClick(vmAddEdit::onConfirmButtonClicked)
            isAnswerCorrectCard.onClick(vmAddEdit::onIsAnswerCorrectCardClicked)
        }
    }

    private fun initObservers() {
        vmAddEdit.isAnswerCorrectStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.checkBox.isChecked = it
        }
    }
}