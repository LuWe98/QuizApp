package com.example.quizapp.view.fragments.adminscreens.managefaculties

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.FragmentAdminAddEditFacultyBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.initMaterialZAxisAnimationForReceiver
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.showSnackBar
import com.example.quizapp.view.fragments.resultdispatcher.setFragmentResultEventListener
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

        binding.pageTitle.setText(vmAddEdit.pageTitleRes)
        initListeners()
        initObservers()
    }

    private fun initListeners() {
        binding.apply {
            btnBack.onClick(vmAddEdit::onBackButtonClicked)
            btnSave.onClick(vmAddEdit::onSaveButtonClicked)
            tvSave.onClick(vmAddEdit::onSaveButtonClicked)
            abbreviationCard.onClick(vmAddEdit::onAbbreviationCardClicked)
            nameCard.onClick(vmAddEdit::onNameCardClicked)
        }
    }

    private fun initObservers() {

        setFragmentResultEventListener(vmAddEdit::onAbbreviationUpdateResultReceived)

        setFragmentResultEventListener(vmAddEdit::onNameUpdateResultReceived)

        vmAddEdit.facultyAbbreviationStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.abbreviationCard.text = if(it.isBlank()) "-" else it
        }

        vmAddEdit.facultyNameStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.nameCard.text = if(it.isBlank()) "-" else it
        }

        vmAddEdit.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }
    }
}