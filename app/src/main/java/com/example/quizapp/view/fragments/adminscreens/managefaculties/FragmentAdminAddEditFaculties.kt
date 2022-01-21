package com.example.quizapp.view.fragments.adminscreens.managefaculties

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.FragmentAdminAddEditFacultyBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.initMaterialZAxisAnimationForReceiver
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.showSnackBar
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmAdminAddEditFaculty
import com.example.quizapp.viewmodel.VmAdminAddEditFaculty.AddEditFacultyEvent.ShowMessageSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminAddEditFaculties : BindingFragment<FragmentAdminAddEditFacultyBinding>() {

    private val vmAddEdit: VmAdminAddEditFaculty by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initMaterialZAxisAnimationForReceiver()
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            pageTitle.setText(vmAddEdit.pageTitleRes)
            abbreviationTextInput.text = vmAddEdit.facultyAbbreviation
            nameTextInput.text = vmAddEdit.facultyName
        }
    }

    private fun initListeners() {
        binding.apply {
            btnBack.onClick(vmAddEdit::onBackButtonClicked)
            btnSave.onClick(vmAddEdit::onSaveButtonClicked)
            tvSave.onClick(vmAddEdit::onSaveButtonClicked)

            abbreviationTextInput.onTextChanged(vmAddEdit::onAbbreviationUpdated)
            nameTextInput.onTextChanged(vmAddEdit::onNameChanged)
        }
    }

    private fun initObservers() {
        vmAddEdit.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }
    }
}