package com.example.quizapp.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.DfShareQuestionnaireBinding
import com.example.quizapp.extensions.observe
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onTextChanged
import com.example.quizapp.extensions.showSnackBar
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
        binding.apply {
            etUserName.setText(vmShare.userName)
        }
    }

    private fun initListeners() {
        binding.apply {
            etUserName.onTextChanged(vmShare::onUserNameEditTextChanged)
            btnCancel.onClick(navigator::popBackStack)
            btnShare.onClick(vmShare::onShareButtonClicked)
        }
    }

    private fun initObservers(){
        vmShare.dfShareQuestionnaireEventChannelFlow.observe(viewLifecycleOwner) { event ->
            when(event){
                is ShowMessageSnackBar -> showSnackBar(event.message)
                NavigateBackEvent -> navigator.popBackStack()
            }
        }
    }
}