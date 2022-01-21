package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.ListLoadItemType
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaHomeQuestionnaires
import com.example.quizapp.viewmodel.VmHome
import com.example.quizapp.viewmodel.VmHome.FragmentHomeEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHome : BindingFragment<FragmentHomeBinding>() {

    private val vmHome: VmHome by hiltNavDestinationViewModels(R.id.fragmentHome)

    private lateinit var rvAdapter: RvaHomeQuestionnaires

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            etSearchQuery.setText(vmHome.searchQuery)

            rvAdapter = RvaHomeQuestionnaires().apply {
                onItemClick = vmHome::onQuestionnaireClicked
                onItemLongClick = vmHome::onQuestionnaireLongClicked
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
            ivSettings.onClick(vmHome::onSettingsButtonClicked)
            ivSearch2.onClick(vmHome::onRemoteSearchButtonClicked)
            btnAdd.onClick(vmHome::onAddQuestionnaireButtonClicked)
//            cardSettings.onClick(vmHome::onSettingsButtonClicked)
//            cardSearch.onClick(vmHome::onRemoteSearchButtonClicked)
//            addCard.onClick(vmHome::onAddQuestionnaireButtonClicked)

            ivFilter.onClick(vmHome::onFilterButtonClicked)

            btnSearch.onClick(vmHome::onClearSearchQueryClicked)
            btnFilter.onClick(vmHome::onFilterButtonClicked)
            etSearchQuery.onTextChanged(vmHome::onSearchQueryChanged)
            swipeRefreshLayout.setOnRefreshListener(vmHome::onSwipeRefreshTriggered)
        }
    }

    private fun initObservers() {
        vmHome.completeQuestionnaireFlow.collectWhenStarted(viewLifecycleOwner) {
            it.adjustVisibilities(
                binding.rv,
                binding.dataAvailability,
                ListLoadItemType.LOCAL_QUESTIONNAIRE
            )
            rvAdapter.submitList(it.data)
        }

        vmHome.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isEmpty()
            }
        }

        vmHome.locallyPresentAuthors.collectWhenStarted(viewLifecycleOwner) {
            vmHome.onLocallyPresentAuthorsChanged(it)
        }

        vmHome.userNameFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.tvHello.text = getString(R.string.helloHome, it)
        }

        vmHome.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowMessageSnackBar -> showSnackBar(textRes = event.messageRes)
                is ShowUndoDeleteSnackBar -> showSnackBar(
                    textRes = event.messageRes,
                    onDismissedAction = event::executeConfirmAction,
                    actionTextRes = R.string.undo,
                    actionClickEvent = event::executeUndoAction
                )
                is ChangeProgressVisibility -> binding.swipeRefreshLayout.isRefreshing = event.visible
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}