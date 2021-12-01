package com.example.quizapp.view.fragments.searchscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentSearchBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.searchscreen.filterselection.BsdfBrowseQuestionnaireFilterSelection
import com.example.quizapp.view.fragments.searchscreen.filterselection.BrowseQuestionnaireFilterSelectionResult
import com.example.quizapp.view.recyclerview.adapters.RvaBrowsableQuestionnaires
import com.example.quizapp.viewmodel.VmSearch
import com.example.quizapp.viewmodel.VmSearch.SearchEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSearch : BindingFragment<FragmentSearchBinding>() {

    private val vmSearch: VmSearch by hiltNavDestinationViewModels(R.id.fragmentSearch)

    private lateinit var rvAdapter: RvaBrowsableQuestionnaires

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            etSearchQuery.setText(vmSearch.searchQuery)

            rvAdapter = RvaBrowsableQuestionnaires(vmSearch).apply {
                onDownloadClick = vmSearch::onItemDownLoadButtonClicked
                onItemClicked = vmSearch::onItemClicked
                onLongClicked = vmSearch::onItemLongClicked
            }

            rv.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = rvAdapter
                disableChangeAnimation()
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            btnBack.onClick(navigator::popBackStack)
            btnSearch.onClick(vmSearch::onClearSearchQueryClicked)
            btnFilter.onClick(vmSearch::onFilterButtonClicked)
            etSearchQuery.onTextChanged(vmSearch::onSearchQueryChanged)
            swipeRefreshLayout.setOnRefreshListener(rvAdapter::refresh)
        }
    }

    private fun initObservers() {
        setFragmentResultListener(BsdfBrowseQuestionnaireFilterSelection.QUESTIONNAIRE_FILTER_RESULT_KEY) { key, bundle ->
            bundle.getParcelable<BrowseQuestionnaireFilterSelectionResult>(key)?.let(vmSearch::onQuestionnaireFilterUpdateReceived)
        }

        setSelectionTypeWithParsedValueListener(vmSearch::onMoreOptionsItemClickedUpdateReceived)


        vmSearch.filteredPagedData.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitData(it)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        vmSearch.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isBlank()
            }
        }

        vmSearch.searchEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is NavigateToQuestionnaireFilterSelection -> navigator.navigateToQuestionnaireFilterDialog(
                    event.selectedCosIds,
                    event.selectedFaculties,
                    event.selectedAuthors
                )
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
                is ChangeItemDownloadStatusEvent -> rvAdapter.changeItemDownloadStatus(event.questionnaireId, event.status)
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
                is NavigateToSelectionScreen -> navigator.navigateToSelectionDialog(event.selectionType)
                HideLoadingDialog -> navigator.popLoadingDialog()
                is ShowLoadingDialog -> navigator.navigateToLoadingDialog(event.messageRes)
                is NavigateToQuizScreen -> navigator.navigateToQuizScreen(event.questionnaireId)
            }
        }
    }
}