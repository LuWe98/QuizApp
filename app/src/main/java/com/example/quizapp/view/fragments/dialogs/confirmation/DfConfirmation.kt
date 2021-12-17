package com.example.quizapp.view.fragments.dialogs.confirmation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.DfConfirmationBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmConfirmationDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfConfirmation : BindingDialogFragment<DfConfirmationBinding>() {

    private val vmConfirmation: VmConfirmationDialog by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.apply {
            vmConfirmation.confirmationType.apply {
                tvTitle.setText(titleRes)
                tvText.setText(textRes)
                btnConfirm.setText(positiveButtonRes)
                btnCancel.setText(negativeButtonRes)
            }

            btnCancel.onClick(vmConfirmation::onCancelButtonClicked)
            btnConfirm.onClick(vmConfirmation::onConfirmButtonClicked)
        }
    }
}