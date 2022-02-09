package com.example.quizapp.view.fragments.authscreen

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAuthLoginBinding
import com.example.quizapp.extensions.getColor
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onTextChanged
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
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

            SpannableString(getString(R.string.registerHere)).let { spannableText ->
                spannableText.setSpan(
                    ForegroundColorSpan(getColor(R.color.hfuDarkerGreen)),
                    spannableText.indexOf("?") + 1,
                    spannableText.length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                btnGoToRegister.text = spannableText
            }
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