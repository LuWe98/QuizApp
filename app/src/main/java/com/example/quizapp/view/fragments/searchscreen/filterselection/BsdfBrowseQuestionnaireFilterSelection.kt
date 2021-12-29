package com.example.quizapp.view.fragments.searchscreen.filterselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfBrowseQuestionnaireFilterSelectionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.fragments.resultdispatcher.setFragmentResultEventListener
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.viewmodel.VmBrowseQuestionnaireFilterSelection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfBrowseQuestionnaireFilterSelection : BindingBottomSheetDialogFragment<BsdfBrowseQuestionnaireFilterSelectionBinding>() {

    private val vmFilter: VmBrowseQuestionnaireFilterSelection by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initListeners()
        initObservers()
    }

    private fun initListeners() {
        binding.apply {
            tvOrderBy.onClick(vmFilter::onOrderByCardClicked)
            tvOrderAscending.onClick(vmFilter::onOrderAscendingCardClicked)
            addChipAuthor.onClick(vmFilter::onAuthorAddButtonClicked)
            addChipCos.onClick(vmFilter::onCourseOfStudiesAddButtonClicked)
            addChipFaculty.onClick(vmFilter::onFacultyCardAddButtonClicked)
            btnApply.onClick(vmFilter::onApplyButtonClicked)
        }
    }

    private fun initObservers() {

        setFragmentResultEventListener(vmFilter::onAuthorsSelectionResultReceived)

        setFragmentResultEventListener(vmFilter::onFacultiesSelectionResultReceived)

        setFragmentResultEventListener(vmFilter::onCourseOfStudiesSelectionResultReceived)

        setFragmentResultEventListener(vmFilter::onRemoteOrderBySelectionResultReceived)

        vmFilter.orderByStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                ivSortBy.setImageDrawable(it.iconRes)
                tvOrderBy.setText(it.textRes)
            }
        }

        vmFilter.orderAscendingStateFlow.collectWhenStarted(viewLifecycleOwner) { ascending ->
            binding.apply {
                tvOrderAscending.setText(if(ascending) R.string.orderAscending else R.string.orderDescending)
                tvOrderAscending.clearAnimation()
                ivOrderAscending.animate()
                    .rotation(if(ascending) 0f else 180f)
                    .setDuration(300)
                    .start()
            }
        }

        vmFilter.selectedAuthorsStateFlow.collectWhenStarted(viewLifecycleOwner) { authors ->
            binding.chipGroupAuthor.setUpChipsForChipGroup(
                authors,
                AuthorInfo::userName,
                vmFilter::removeFilteredAuthor
            ) { showToast(it.userName) }
        }

        vmFilter.selectedCourseOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) { coursesOfStudies ->
            binding.chipGroupCos.setUpChipsForChipGroup(
                coursesOfStudies,
                CourseOfStudies::abbreviation,
                vmFilter::removeFilteredCourseOfStudies
            ) { showToast(it.name) }
        }

        vmFilter.selectedFacultyStateFlow.collectWhenStarted(viewLifecycleOwner) { faculties ->
            binding.chipGroupFaculty.setUpChipsForChipGroup(
                faculties,
                Faculty::abbreviation,
                vmFilter::removeFilteredFaculty
            ) { showToast(it.name) }
        }
    }
}