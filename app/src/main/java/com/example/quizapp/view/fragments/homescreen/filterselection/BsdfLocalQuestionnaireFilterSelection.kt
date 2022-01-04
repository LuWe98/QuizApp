package com.example.quizapp.view.fragments.homescreen.filterselection

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfLocalQuestionnaireFilterSelectionBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.view.recyclerview.adapters.RvaAuthorChoice
import com.example.quizapp.view.recyclerview.adapters.RvaCourseOfStudiesChoice
import com.example.quizapp.view.recyclerview.adapters.RvaFacultyChoice
import com.example.quizapp.viewmodel.VmLocalQuestionnaireFilterSelection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfLocalQuestionnaireFilterSelection: BindingBottomSheetDialogFragment<BsdfLocalQuestionnaireFilterSelectionBinding>() {

    private val vmFilter: VmLocalQuestionnaireFilterSelection by viewModels()

    private lateinit var rvaAuthor: RvaAuthorChoice

    private lateinit var rvaCourseOfStudiesChoice: RvaCourseOfStudiesChoice

    private lateinit var rvaFaculty: RvaFacultyChoice


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        rvaAuthor = RvaAuthorChoice().apply {
            onDeleteButtonClicked = vmFilter::removeFilteredAuthor
        }

        rvaCourseOfStudiesChoice = RvaCourseOfStudiesChoice().apply {
            onDeleteButtonClicked = vmFilter::removeFilteredCourseOfStudies
        }

        rvaFaculty = RvaFacultyChoice().apply {
            onDeleteButtonClicked = vmFilter::removeFilteredFaculty
        }

        binding.apply {
            rvAuthors.apply {
                adapter = rvaAuthor
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(false)
                disableChangeAnimation()
            }

            rvCos.apply {
                adapter = rvaCourseOfStudiesChoice
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(false)
                disableChangeAnimation()
            }

            rvFaculty.apply {
                adapter = rvaFaculty
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(false)
                disableChangeAnimation()
            }
        }
    }

    private fun initListeners(){
        binding.apply {
            tvOrderBy.onClick(vmFilter::onOrderByCardClicked)
            tvOrderAscending.onClick(vmFilter::onOrderAscendingCardClicked)
            btnApply.onClick(vmFilter::onApplyButtonClicked)
            tvHideCompleted.onClick(vmFilter::onHideCompletedCardClicked)
            btnCollapse.onClick(vmFilter::onCollapseButtonClicked)

            btnBrowseAuthor.onClick(vmFilter::onAuthorAddButtonClicked)
            btnBrowseCourseOfStudies.onClick(vmFilter::onCourseOfStudiesAddButtonClicked)
            btnBrowseFaculty.onClick(vmFilter::onFacultyCardAddButtonClicked)
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
            rvaAuthor.submitList(authors) {
                binding.rvAuthors.isVisible = authors.isNotEmpty()
            }
        }

        vmFilter.selectedCosStateFlow.collectWhenStarted(viewLifecycleOwner) { cos ->
            rvaCourseOfStudiesChoice.submitList(cos) {
                binding.rvCos.isVisible = cos.isNotEmpty()
            }
        }

        vmFilter.selectedFacultyStateFlow.collectWhenStarted(viewLifecycleOwner) { faculties ->
            rvaFaculty.submitList(faculties) {
                binding.rvFaculty.isVisible = faculties.isNotEmpty()
            }
        }

        vmFilter.selectedHideCompletedStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.hideCompletedCheckBox.isChecked = it
        }
    }
}