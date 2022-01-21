package com.example.quizapp.view.fragments.searchscreen.filterselection

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfBrowseQuestionnaireFilterSelectionBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.view.recyclerview.adapters.RvaAuthorChoice
import com.example.quizapp.view.recyclerview.adapters.RvaCourseOfStudiesChoice
import com.example.quizapp.view.recyclerview.adapters.RvaFacultyChoice
import com.example.quizapp.viewmodel.VmBrowseQuestionnaireFilterSelection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfBrowseQuestionnaireFilterSelection : BindingBottomSheetDialogFragment<BsdfBrowseQuestionnaireFilterSelectionBinding>() {

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
            rvAuthor.apply {
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
            tvOrderBy.onClick(vmFilter::onOrderByCardClicked)
            tvOrderAscending.onClick(vmFilter::onOrderAscendingCardClicked)
            btnApply.onClick(vmFilter::onApplyButtonClicked)
            btnCollapse.onClick(vmFilter::onCollapseButtonClicked)

            btnAddAuthor.onClick(vmFilter::onAuthorAddButtonClicked)
            btnAddCos.onClick(vmFilter::onCourseOfStudiesAddButtonClicked)
            btnAddFaculty.onClick(vmFilter::onFacultyCardAddButtonClicked)

            btnClearAuthors.onClick(vmFilter::onClearAuthorFilterClicked)
            btnClearCos.onClick(vmFilter::onClearCourseOfStudiesFilterClicked)
            btnClearFaculty.onClick(vmFilter::onClearFacultyFilterClicked)
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
            rvaAuthor.submitList(authors.toMutableList()) {
                binding.rvAuthor.isVisible = authors.isNotEmpty()
            }
        }

        vmFilter.selectedCourseOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) { cos ->
            rvaCourseOfStudiesChoice.submitList(cos) {
                binding.rvCos.isVisible = cos.isNotEmpty()
            }
        }

        vmFilter.selectedFacultyStateFlow.collectWhenStarted(viewLifecycleOwner) { faculties ->
            rvaFaculty.submitList(faculties) {
                binding.rvFaculty.isVisible = faculties.isNotEmpty()
            }
        }
    }
}