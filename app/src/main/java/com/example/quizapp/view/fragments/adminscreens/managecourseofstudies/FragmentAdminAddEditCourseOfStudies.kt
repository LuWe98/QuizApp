package com.example.quizapp.view.fragments.adminscreens.managecourseofstudies

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.FragmentAdminAddEditCourseOfStudiesBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.facultyselection.BsdfFacultySelection
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.DfUpdateStringValueType
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies.AddEditCourseOfStudiesEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminAddEditCourseOfStudies: BindingFragment<FragmentAdminAddEditCourseOfStudiesBinding>() {

    private val vmAddEdit : VmAdminAddEditCourseOfStudies by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        }
    }

    private fun initObservers(){
        setFragmentResultListener(DfUpdateStringValueType.UPDATE_COURSE_OF_STUDIES_ABBREVIATION_RESULT_KEY) { key, bundle ->
            bundle.getString(key)?.let(vmAddEdit::onAbbreviationUpdateReceived)
        }

        setFragmentResultListener(DfUpdateStringValueType.UPDATE_COURSE_OF_STUDIES_NAME_RESULT_KEY) { key, bundle ->
            bundle.getString(key)?.let(vmAddEdit::onNameUpdateReceived)
        }

        setFragmentResultListener(BsdfFacultySelection.FACULTY_SELECTION_RESULT_KEY) { _, bundle ->
            bundle.getStringArray(BsdfFacultySelection.SELECTED_FACULTIES_KEY)?.let(vmAddEdit::onFacultyIdsUpdateReceived)
        }

        vmAddEdit.cosAbbreviationStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.abbreviationCard.text = it
        }

        vmAddEdit.cosNameStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.nameCard.text = it
        }

        vmAddEdit.cosFacultyIdsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.facultiesCard.text = it.map(Faculty::abbreviation).reduceOrNull { acc, abbr -> "$acc, $abbr" } ?: ""
        }

        vmAddEdit.addEditCourseOfStudiesEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToUpdateStringDialog -> navigator.navigateToUpdateStringValueDialog(event.initialValue, event.updateType)
                is NavigateToFacultySelectionScreen -> navigator.navigateToFacultySelection(event.selectedIds.toSet())
                is ShowMessageSnackBar -> {

                }
                NavigateBackEvent -> navigator.popBackStack()
            }
        }
    }
}