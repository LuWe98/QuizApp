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

        binding.pageTitle.setText(vmAddEdit.pageTitleRes)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        rvaFaculty = RvaFacultyChoice().apply {
            onDeleteButtonClicked = vmAddEdit::onFacultyChipClicked
        }

        binding.rvFaculty.apply {
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
            abbreviationCard.onClick(vmAddEdit::onAbbreviationCardClicked)
            nameCard.onClick(vmAddEdit::onNameCardClicked)
            btnAddFaculty.onClick(vmAddEdit::onFacultyCardClicked)
            degreeCard.onClick(vmAddEdit::onDegreeCardClicked)
        }
    }

    private fun initObservers(){

        setFragmentResultEventListener(vmAddEdit::onNameUpdateResultReceived)

        setFragmentResultEventListener(vmAddEdit::onAbbreviationUpdateResultReceived)

        setFragmentResultEventListener(vmAddEdit::onFacultySelectionResultReceived)

        setFragmentResultEventListener(vmAddEdit::onDegreeSelectionResultReceived)

        vmAddEdit.cosAbbreviationStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.abbreviationCard.text = if(it.isBlank()) "-" else it
        }

        vmAddEdit.cosNameStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.nameCard.text = if(it.isBlank()) "-" else it
        }

        vmAddEdit.cosFacultyIdsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvaFaculty.submitList(it) {
                binding.rvFaculty.isVisible = it.isNotEmpty()
            }
        }

        vmAddEdit.cosDegreeStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.degreeCard.text = getString(it.textRes)
        }

        vmAddEdit.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }
    }
}