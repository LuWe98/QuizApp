package com.example.quizapp.view.fragments.dialogs.sharequestionnaire

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.DfShareQuestionnaireBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmShareQuestionnaire
import com.example.quizapp.viewmodel.VmShareQuestionnaire.*
import com.example.quizapp.viewmodel.VmShareQuestionnaire.ShareQuestionnaireEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfShareQuestionnaire : BindingDialogFragment<DfShareQuestionnaireBinding>() {

    private val vmShare: VmShareQuestionnaire by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        binding.apply {
            etUserName.setText(vmShare.userName)

            showKeyboard(etUserName)
            etUserName.requestFocus()
        }
    }

    private fun initListeners() {
        binding.apply {
            etUserName.onTextChanged(vmShare::onUserNameEditTextChanged)
            btnConfirm.onClick(vmShare::onShareButtonClicked)
            btnCancel.onClick(navigator::popBackStack)
        }
    }

    private fun initObservers(){
        vmShare.shareQuestionnaireEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event){
                is ShowMessageSnackBar -> showSnackBar(event.message)
                NavigateBackEvent -> navigator.popBackStack()
                HideLoadingDialog -> navigator.popLoadingDialog()
                is ShowLoadingDialog -> {
                    navigator.navigateToLoadingDialog(event.messageRes)
                    binding.root.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .start()
                }
            }
        }
    }
}