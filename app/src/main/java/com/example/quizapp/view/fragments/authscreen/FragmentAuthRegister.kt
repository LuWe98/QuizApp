package com.example.quizapp.view.fragments.authscreen

import android.os.Bundle
import android.view.View
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAuthRegisterBinding
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onTextChanged
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAuthRegister : BindingFragment<FragmentAuthRegisterBinding>() {

    private val viewModel: VmAuth by hiltNavDestinationViewModels(R.id.fragmentAuth)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews() {
        binding.apply {
            etUserName.setText(viewModel.currentRegisterUserName)
            etPassword.setText(viewModel.currentRegisterPassword)
            etConfirmPassword.setText(viewModel.currentRegisterPasswordConfirm)
        }
    }

    private fun initListeners() {
        binding.apply {
            btnGoToLogin.onClick(viewModel::onGoToLoginButtonClicked)
            btnRegister.onClick(viewModel::onRegisterButtonClicked)
            etUserName.onTextChanged(viewModel::onRegisterEmailEditTextChanged)
            etPassword.onTextChanged(viewModel::onRegisterPasswordEditTextChanged)
            etConfirmPassword.onTextChanged(viewModel::onRegisterPasswordConfirmEditTextChanged)
        }
    }
}