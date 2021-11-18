package com.example.quizapp.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.DfShareQuestionnaireBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmShareQuestionnaire
import com.example.quizapp.viewmodel.VmShareQuestionnaire.DfShareQuestionnaireEvent.NavigateBackEvent
import com.example.quizapp.viewmodel.VmShareQuestionnaire.DfShareQuestionnaireEvent.ShowMessageSnackBar
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
            btnShare.onClick(vmShare::onShareButtonClicked)
        }
    }

    private fun initObservers(){
        vmShare.dfShareQuestionnaireEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event){
                is ShowMessageSnackBar -> showSnackBar(event.message)
                NavigateBackEvent -> navigator.popBackStack()
            }
        }
    }
}