package com.example.quizapp.view.fragments.adminscreens.managecourseofstudies

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.FragmentAdminAddEditCourseOfStudiesBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.fragments.resultdispatcher.setFragmentResultEventListener
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies.AddEditCourseOfStudiesEvent.ShowMessageSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminAddEditCourseOfStudies: BindingFragment<FragmentAdminAddEditCourseOfStudiesBinding>() {

    private val vmAddEdit : VmAdminAddEditCourseOfStudies by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        binding.pageTitle.setText(vmAddEdit.pageTitleRes)
        initListeners()
        initObservers()
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(vmAddEdit::onBackButtonClicked)
            btnSave.onClick(vmAddEdit::onSaveButtonClicked)
            abbreviationCard.onClick(vmAddEdit::onAbbreviationCardClicked)
            nameCard.onClick(vmAddEdit::onNameCardClicked)
            facultiesCard.onClick(vmAddEdit::onFacultyCardClicked)
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
            binding.facultiesCard.text = it.map(Faculty::abbreviation).reduceOrNull { acc, abbr -> "$acc, $abbr" } ?: "-"
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