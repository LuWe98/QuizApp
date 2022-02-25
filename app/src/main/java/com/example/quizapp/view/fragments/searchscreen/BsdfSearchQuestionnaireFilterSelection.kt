package com.example.quizapp.view.fragments.searchscreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.BsdfSearchQuestionnaireFilterSelectionBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.onClick
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.view.recyclerview.adapters.RvaAuthorChoice
import com.example.quizapp.view.recyclerview.adapters.RvaCourseOfStudiesChoice
import com.example.quizapp.view.recyclerview.adapters.RvaFacultyChoice
import com.example.quizapp.viewmodel.VmBrowseQuestionnaireFilterSelection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfSearchQuestionnaireFilterSelection : BindingBottomSheetDialogFragment<BsdfSearchQuestionnaireFilterSelectionBinding>() {

    private val vmFilter: VmBrowseQuestionnaireFilterSelection by viewModels()

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

    private fun initListeners() {
        binding.apply {
            orderByCard.onClick(vmFilter::onOrderByCardClicked)
            ascendingLayout.onClick(vmFilter::onOrderAscendingCardClicked)
            btnApply.onClick(vmFilter::onApplyButtonClicked)
            btnCollapse.onClick(vmFilter::onCollapseButtonClicked)

            btnAddAuthor.onClick(vmFilter::onAuthorAddButtonClicked)
            btnAddCos.onClick(vmFilter::onCourseOfStudiesAddButtonClicked)
            btnAddFaculty.onClick(vmFilter::onFacultyCardAddButtonClicked)
            tvNoAssignedAuthor.onClick(vmFilter::onAuthorAddButtonClicked)
            tvNoAssignedCos.onClick(vmFilter::onCourseOfStudiesAddButtonClicked)
            tvNoAssignedFaculty.onClick(vmFilter::onFacultyCardAddButtonClicked)
        }
    }

    private fun initObservers() {

        setFragmentResultEventListener(vmFilter::onAuthorsSelectionResultReceived)

        setFragmentResultEventListener(vmFilter::onFacultiesSelectionResultReceived)

        setFragmentResultEventListener(vmFilter::onCourseOfStudiesSelectionResultReceived)

        setFragmentResultEventListener(vmFilter::onRemoteOrderBySelectionResultReceived)

        vmFilter.orderByStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                orderByText.setText(it.textRes)
            }
        }

        vmFilter.orderAscendingStateFlow.collectWhenStarted(viewLifecycleOwner) { ascending ->
            binding.ascendingSwitch.isChecked = ascending
        }

        vmFilter.selectedAuthorsStateFlow.collectWhenStarted(viewLifecycleOwner) { authors ->
            rvaAuthor.submitList(authors.toMutableList()) {
                binding.apply {
                    rvAuthors.isVisible = authors.isNotEmpty()
                    tvNoAssignedAuthor.isVisible = authors.isEmpty()
                }
            }
        }

        vmFilter.selectedCourseOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) { cos ->
            rvaCourseOfStudiesChoice.submitList(cos) {
                binding.apply {
                    rvCos.isVisible = cos.isNotEmpty()
                    tvNoAssignedCos.isVisible = cos.isEmpty()
                }
            }
        }

        vmFilter.selectedFacultyStateFlow.collectWhenStarted(viewLifecycleOwner) { faculties ->
            rvaFaculty.submitList(faculties) {
                binding.apply {
                    rvFaculty.isVisible = faculties.isNotEmpty()
                    tvNoAssignedFaculty.isVisible = faculties.isEmpty()
                }
            }
        }
    }
}