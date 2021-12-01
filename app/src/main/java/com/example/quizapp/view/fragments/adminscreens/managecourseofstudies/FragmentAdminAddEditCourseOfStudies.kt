package com.example.quizapp.view.fragments.adminscreens.managecourseofstudies

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.FragmentAdminAddEditCourseOfStudiesBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.facultyselection.BsdfFacultySelection
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies.AddEditCourseOfStudiesEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminAddEditCourseOfStudies: BindingFragment<FragmentAdminAddEditCourseOfStudiesBinding>() {

    private val vmAddEdit : VmAdminAddEditCourseOfStudies by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pageTitle.setText(vmAddEdit.pageTitleRes)
        initListeners()
        initObservers()
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(navigator::popBackStack)
            btnSave.onClick(vmAddEdit::onSaveButtonClicked)
            abbreviationCard.onClick(vmAddEdit::onAbbreviationCardClicked)
            nameCard.onClick(vmAddEdit::onNameCardClicked)
            facultiesCard.onClick(vmAddEdit::onFacultyCardClicked)
            degreeCard.onClick(vmAddEdit::onDegreeCardClicked)
        }
    }

    private fun initObservers(){
        setUpdateStringTypeListener(UpdateStringType.COURSE_OF_STUDIES_ABBREVIATION, vmAddEdit::onAbbreviationUpdateReceived)

        setUpdateStringTypeListener(UpdateStringType.COURSE_OF_STUDIES_NAME, vmAddEdit::onNameUpdateReceived)

        setFragmentResultListener(BsdfFacultySelection.FACULTY_SELECTION_RESULT_KEY) { key, bundle ->
            bundle.getStringArray(key)?.let(vmAddEdit::onFacultyIdsUpdateReceived)
        }

        setSelectionTypeListener(vmAddEdit::onDegreeResultReceived)

        vmAddEdit.cosAbbreviationStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.abbreviationCard.text = if(it.isBlank()) "-" else it
        }

        vmAddEdit.cosNameStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.nameCard.text = if(it.isBlank()) "-" else it
        }

        vmAddEdit.cosFacultyIdsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.facultiesCard.text = it.map(Faculty::abbreviation).reduceOrNull { acc, abbr -> "$acc, $abbr" } ?: ""
        }

        vmAddEdit.cosDegreeStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.degreeCard.text = getString(it.textRes)
        }

        vmAddEdit.addEditCourseOfStudiesEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToUpdateStringDialog -> navigator.navigateToUpdateStringDialog(event.initialValue, event.updateType)
                is NavigateToFacultySelectionScreen -> navigator.navigateToFacultySelection(event.selectedIds)
                is NavigateToDegreeSelectionScreen -> navigator.navigateToSelectionDialog(SelectionType.DegreeSelection(event.currentDegree))
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
                NavigateBackEvent -> navigator.popBackStack()
                HideLoadingDialog -> navigator.popLoadingDialog()
                is ShowLoadingDialog -> navigator.navigateToLoadingDialog(event.messageRes)
            }
        }
    }
}