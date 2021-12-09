package com.example.quizapp.view.fragments.dialogs.changepassword

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.DfChangePasswordBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onTextChanged
import com.example.quizapp.extensions.showSnackBar
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmChangePassword
import com.example.quizapp.viewmodel.VmChangePassword.ChangePasswordEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfChangePassword: BindingDialogFragment<DfChangePasswordBinding>() {

    private val vmChangePw: VmChangePassword by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            etCurrentPassword.setText(vmChangePw.currentPw)
            etNewPassword.setText(vmChangePw.newPw)
        }
    }

    private fun initListeners(){
        binding.apply {
            etCurrentPassword.onTextChanged(vmChangePw::onCurrentPwChanged)
            etNewPassword.onTextChanged(vmChangePw::onNewPasswordChanged)
            btnCancel.onClick(navigator::popBackStack)
            btnConfirm.onClick(vmChangePw::onConfirmButtonClicked)
        }
    }

    private fun initObservers(){
        vmChangePw.changePasswordEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                HideLoadingDialog -> navigator.popLoadingDialog()
                NavigateBackEvent -> navigator.popBackStack()
                is ShowLoadingDialog -> navigator.navigateToLoadingDialog(event.messageRes)
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }
    }
}