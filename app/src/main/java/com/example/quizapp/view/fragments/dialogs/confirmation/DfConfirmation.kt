package com.example.quizapp.view.fragments.dialogs.confirmation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.example.quizapp.databinding.DfConfirmationBinding
import com.example.quizapp.extensions.onClick
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfConfirmation: BindingDialogFragment<DfConfirmationBinding>() {

    private val args: DfConfirmationArgs by navArgs()

    private val confirmationType get() = args.confirmationType

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews(){
        binding.apply {
            tvTitle.setText(confirmationType.titleRes)
            tvText.setText(confirmationType.textRes)
            btnConfirm.setText(confirmationType.positiveButtonRes)
            btnCancel.setText(confirmationType.negativeButtonRes)

            btnCancel.onClick(navigator::popBackStack)
            btnConfirm.onClick {
                setFragmentResult(confirmationType.resultKey, Bundle().apply {
                    putParcelable(confirmationType.resultKey, confirmationType)
                })
                navigator.popBackStack()
            }
        }
    }
}