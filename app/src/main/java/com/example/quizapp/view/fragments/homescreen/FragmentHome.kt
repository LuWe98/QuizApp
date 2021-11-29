package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaCreatedQuestionnaires
import com.example.quizapp.viewmodel.VmHome
import com.example.quizapp.viewmodel.VmHome.FragmentHomeEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHome : BindingFragment<FragmentHomeBinding>() {

    private val vmHome: VmHome by hiltNavDestinationViewModels(R.id.fragmentHome)

    private lateinit var rvAdapter: RvaCreatedQuestionnaires

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            etSearchQuery.setText(vmHome.searchQuery)

            rvAdapter = RvaCreatedQuestionnaires().apply {
                onItemClick = navigator::navigateToQuizScreen
                onItemLongClick = navigator::navigateToQuestionnaireMoreOptions
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
            cardSettings.onClick(navigator::navigateToSettingsScreen)
            cardSearch.onClick(navigator::navigateToSearchScreen)
            addCard.onClick(navigator::navigateToAddEditQuestionnaireScreen)
            btnSearch.onClick(vmHome::onClearSearchQueryClicked)
            btnFilter.onClick(vmHome::onFilterButtonClicked)
            etSearchQuery.onTextChanged(vmHome::onSearchQueryChanged)
            swipeRefreshLayout.setOnRefreshListener(vmHome::onSwipeRefreshTriggered)
        }
    }

    private fun initObservers() {
        vmHome.allCompleteQuestionnaireFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }

        vmHome.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isBlank()
            }
        }

        vmHome.fragmentHomeEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowSnackBarMessageBar -> showSnackBar(event.messageRes)
                is ShowUndoDeleteCreatedQuestionnaireSnackBar -> {
                    showSnackBar(
                        R.string.questionnaireDeleted,
                        anchorView = binding.bottomNavContainer,
                        onDismissedAction = { vmHome.onDeleteCreatedQuestionnaireConfirmed(event) },
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmHome.onUndoDeleteCreatedQuestionnaireClicked(event) }
                    )
                }
                is ShowUndoDeleteCachedQuestionnaireSnackBar -> {
                    showSnackBar(
                        R.string.questionnaireDeleted,
                        anchorView = binding.bottomNavContainer,
                        onDismissedAction = { vmHome.onDeleteCachedQuestionnaireConfirmed(event) },
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmHome.onUndoDeleteCachedQuestionnaireClicked(event) }
                    )
                }
                is ShowUndoDeleteAnswersOfQuestionnaireSnackBar -> {
                    showSnackBar(
                        R.string.answersDeleted,
                        anchorView = binding.bottomNavContainer,
                        onDismissedAction = { vmHome.onDeleteFilledQuestionnaireConfirmed(event) },
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmHome.onUndoDeleteFilledQuestionnaireClicked(event) }
                    )
                }
                is ChangeProgressVisibility -> binding.swipeRefreshLayout.isRefreshing = event.visible
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
                is NavigateToLocalQuestionnairesFilterSelection -> navigator.navigateToLocalQuestionnaireFilterSelection()
            }
        }
    }
}