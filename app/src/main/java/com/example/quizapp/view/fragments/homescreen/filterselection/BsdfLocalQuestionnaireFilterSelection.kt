package com.example.quizapp.view.fragments.homescreen.filterselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfLocalQuestionnaireFilterSelectionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.fragments.resultdispatcher.setFragmentResultEventListener
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.viewmodel.VmLocalQuestionnaireFilterSelection
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

        setFragmentResultEventListener(vmFilter::onLocalOrderBySelectionResultReceived)

        setFragmentResultEventListener(vmFilter::onAuthorsSelectionResultReceived)

        setFragmentResultEventListener(vmFilter::onCourseOfStudiesSelectionResultReceived)

        setFragmentResultEventListener(vmFilter::onFacultiesSelectionResultReceived)


        vmFilter.selectedOrderByStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                tvOrderBy.setText(it.textRes)
                ivOrderBy.setImageDrawable(it.iconRes)
            }
        }

        vmFilter.selectedOrderAscendingStateFlow.collectWhenStarted(viewLifecycleOwner) { ascending ->
            binding.apply {
                tvOrderAscending.setText(if(ascending) R.string.orderAscending else R.string.orderDescending)
                ivOrderAscending.clearAnimation()
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

        vmFilter.selectedCosStateFlow.collectWhenStarted(viewLifecycleOwner) { cos ->
            binding.chipGroupCos.setUpChipsForChipGroup(
                cos,
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

        vmFilter.selectedHideCompletedStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.hideCompletedCheckBox.isChecked = it
        }
    }
}