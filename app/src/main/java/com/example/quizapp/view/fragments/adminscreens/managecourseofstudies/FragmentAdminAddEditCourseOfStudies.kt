package com.example.quizapp.view.fragments.adminscreens.managecourseofstudies

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.FragmentAdminAddEditCourseOfStudiesBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.view.recyclerview.adapters.RvaFacultyChoice
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies.AddEditCourseOfStudiesEvent.ShowMessageSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminAddEditCourseOfStudies: BindingFragment<FragmentAdminAddEditCourseOfStudiesBinding>() {

    private val vmAddEdit : VmAdminAddEditCourseOfStudies by viewModels()

    private lateinit var rvaFaculty: RvaFacultyChoice

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            pageTitle.setText(vmAddEdit.pageTitleRes)
            contentLayout.etAbbreviation.setText(vmAddEdit.cosAbbreviation)
            contentLayout.etName.setText(vmAddEdit.cosName)
        }

        rvaFaculty = RvaFacultyChoice().apply {
            onDeleteButtonClicked = vmAddEdit::onFacultyChipClicked
        }

        binding.contentLayout.rvFaculties.apply {
            adapter = rvaFaculty
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            disableChangeAnimation()
        }
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(vmAddEdit::onBackButtonClicked)
            btnSave.onClick(vmAddEdit::onSaveButtonClicked)
            tvSave.onClick(vmAddEdit::onSaveButtonClicked)
            contentLayout.apply {
                degreeCard.onClick(vmAddEdit::onDegreeCardClicked)

                btnAddFaculty.onClick(vmAddEdit::onFacultyCardClicked)
                //btnClearFaculties.onClick(vmAddEdit::onClearFacultiesClicked)

                etAbbreviation.onTextChanged(vmAddEdit::onAbbreviationUpdated)
                etName.onTextChanged(vmAddEdit::onNameChanged)
            }
        }
    }

    private fun initObservers(){

        setFragmentResultEventListener(vmAddEdit::onFacultySelectionResultReceived)

        setFragmentResultEventListener(vmAddEdit::onDegreeSelectionResultReceived)

        vmAddEdit.cosFacultyIdsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvaFaculty.submitList(it) {
                binding.contentLayout.apply {
                    rvFaculties.isVisible = it.isNotEmpty()
                    tvNoAssigned.isVisible = it.isEmpty()
                }
            }
        }

        vmAddEdit.cosDegreeStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.contentLayout.degreeText.text = getString(it.textRes)
        }

        vmAddEdit.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }
    }
}