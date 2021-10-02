package com.example.quizapp.view.fragments.authscreen

import android.os.Bundle
import android.view.View
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAuthLoginBinding
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onTextChanged
import com.example.quizapp.view.fragments.bindingfragmentsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAuthLogin : BindingFragment<FragmentAuthLoginBinding>() {

    private val viewModel : VmAuth by hiltNavDestinationViewModels(R.id.fragmentAuth)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews(){
        binding.apply {
            etUserName.setText(viewModel.currentLoginUserName)
            etPassword.setText(viewModel.currentLoginPassword)
        }
    }

    private fun initListeners(){
        binding.apply {
            btnGoToRegister.onClick(viewModel::onGoToRegisterButtonClicked)
            btnLogin.onClick(viewModel::onLoginButtonClicked)
            etUserName.onTextChanged(viewModel::onLoginEmailEditTextChanged)
            etPassword.onTextChanged(viewModel::onLoginPasswordEditTextChanged)
        }
    }
}