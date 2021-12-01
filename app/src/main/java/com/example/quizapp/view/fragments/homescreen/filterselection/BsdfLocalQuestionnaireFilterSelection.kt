package com.example.quizapp.view.fragments.homescreen.filterselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfLocalQuestionnaireFilterSelectionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.fragments.dialogs.authorselection.local.BsdfLocalAuthorSelection
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.BsdfCourseOfStudiesSelection
import com.example.quizapp.view.fragments.dialogs.facultyselection.BsdfFacultySelection
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmLocalQuestionnaireFilterSelection
import com.example.quizapp.viewmodel.VmLocalQuestionnaireFilterSelection.LocalQuestionnaireFilterSelectionEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfLocalQuestionnaireFilterSelection: BindingBottomSheetDialogFragment<BsdfLocalQuestionnaireFilterSelectionBinding>() {

    private val vmFilter: VmLocalQuestionnaireFilterSelection by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initListeners()
        initObservers()
    }

    private fun initListeners(){
        binding.apply {
            tvOrderBy.onClick(vmFilter::onOrderByCardClicked)
            tvOrderAscending.onClick(vmFilter::onOrderAscendingCardClicked)
            btnApply.onClick(vmFilter::onApplyButtonClicked)
            addChipAuthor.onClick(vmFilter::onAuthorAddButtonClicked)
            addChipCos.onClick(vmFilter::onCourseOfStudiesAddButtonClicked)
            addChipFaculty.onClick(vmFilter::onFacultyCardAddButtonClicked)
            tvHideCompleted.onClick(vmFilter::onHideCompletedCardClicked)
        }
    }

    private fun initObservers(){
        setSelectionTypeListener(vmFilter::onOrderByUpdateReceived)

        setFragmentResultListener(BsdfLocalAuthorSelection.AUTHOR_SELECTION_RESULT_KEY) { key, bundle ->
            bundle.getStringArray(key)?.let(vmFilter::onSelectedAuthorsUpdateReceived)
        }

        setFragmentResultListener(BsdfFacultySelection.FACULTY_SELECTION_RESULT_KEY) { key, bundle ->
            bundle.getStringArray(key)?.let(vmFilter::onSelectedFacultiesUpdateReceived)
        }

        setFragmentResultListener(BsdfCourseOfStudiesSelection.COURSE_OF_STUDIES_RESULT_KEY) { key, bundle ->
            bundle.getStringArray(key)?.let(vmFilter::onSelectedCourseOfStudiesUpdateReceived)
        }


        vmFilter.selectedOrderByStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                tvOrderBy.setText(it.textRes)
                ivOrderBy.setImageDrawable(it.iconRes)
            }
        }

        vmFilter.selectedOrderAscendingStateFlow.collectWhenStarted(viewLifecycleOwner) { ascending ->
            binding.apply {
                tvOrderAscending.setText(if(ascending) R.string.ascending else R.string.descending)
                ivOrderAscending.clearAnimation()
                ivOrderAscending.animate()
                    .rotation(if(ascending) 0f else 180f)
                    .setDuration(300)
                    .start()
            }
        }

        vmFilter.selectedAuthorsStateFlow.collectWhenStarted(viewLifecycleOwner) { authors ->
            setUpChipsForChipGroup(
                binding.chipGroupAuthor,
                authors.toList(),
                AuthorInfo::userName,
                vmFilter::removeFilteredAuthor
            ) { showToast(it.userName) }
        }

        vmFilter.selectedCosStateFlow.collectWhenStarted(viewLifecycleOwner) { cos ->
            setUpChipsForChipGroup(
                binding.chipGroupCos,
                cos.toList(),
                CourseOfStudies::abbreviation,
                vmFilter::removeFilteredCourseOfStudies
            ) { showToast(it.name) }
        }

        vmFilter.selectedFacultyStateFlow.collectWhenStarted(viewLifecycleOwner) { faculties ->
            setUpChipsForChipGroup(
                binding.chipGroupFaculty,
                faculties.toList(),
                Faculty::abbreviation,
                vmFilter::removeFilteredFaculty
            ) { showToast(it.name) }
        }

        vmFilter.selectedHideCompletedStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.hideCompletedCheckBox.isChecked = it
        }

        vmFilter.localQuestionnaireFilterSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToSelectionScreen -> navigator.navigateToSelectionDialog(event.selectionType)
                is NavigateToLocalAuthorSelectionScreen -> navigator.navigateToLocalAuthorSelection(event.selectedAuthorIds)
                is NavigateToCourseOfStudiesSelectionScreen -> navigator.navigateToCourseOfStudiesSelection(event.selectedCourseOfStudiesIds)
                is NavigateToFacultySelectionScreen -> navigator.navigateToFacultySelection(event.selectedFacultyIds)
                NavigateBackEvent -> navigator.popBackStack()
            }
        }
    }
}