package com.example.quizapp.view.fragments.adminscreens.managefaculties

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.FragmentAdminAddEditFacultyBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setUpdateStringTypeListener
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.example.quizapp.viewmodel.VmAdminAddEditFaculty
import com.example.quizapp.viewmodel.VmAdminAddEditFaculty.AddEditFacultyEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminAddEditFaculties : BindingFragment<FragmentAdminAddEditFacultyBinding>() {

    private val vmAddEdit: VmAdminAddEditFaculty by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pageTitle.setText(vmAddEdit.pageTitleRes)
        initListeners()
        initObservers()
    }

    private fun initListeners() {
        binding.apply {
            btnBack.onClick(navigator::popBackStack)
            btnSave.onClick(vmAddEdit::onSaveButtonClicked)
            abbreviationCard.onClick(vmAddEdit::onAbbreviationCardClicked)
            nameCard.onClick(vmAddEdit::onNameCardClicked)
        }
    }

    private fun initObservers() {
        setUpdateStringTypeListener(UpdateStringType.FACULTY_ABBREVIATION, vmAddEdit::onAbbreviationUpdateReceived)

        setUpdateStringTypeListener(UpdateStringType.FACULTY_NAME, vmAddEdit::onNameUpdateReceived)


        vmAddEdit.facultyAbbreviationStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.abbreviationCard.text = it
        }

        vmAddEdit.facultyNameStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.nameCard.text = it
        }

        vmAddEdit.addEditFacultyEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is NavigateToUpdateStringDialog -> navigator.navigateToUpdateStringDialog(event.initialValue, event.updateType)
                is ShowMessageSnackBar -> {

                }
                NavigateBackEvent -> navigator.popBackStack()
            }
        }
    }
}