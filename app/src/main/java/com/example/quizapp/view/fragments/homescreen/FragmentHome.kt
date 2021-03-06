package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.ListLoadItemType
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
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
            rvAdapter = RvaHomeQuestionnaires().apply {
                onItemClick = vmHome::onQuestionnaireClicked
                onItemLongClick = vmHome::onQuestionnaireLongClicked
                onPlayButtonClick = vmHome::onQuestionnairePlayButtonClicked
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
            ivSearch.onClick(vmHome::onRemoteSearchButtonClicked)
            ivFilter.onClick(vmHome::onFilterButtonClicked)
            btnAdd.onClick(vmHome::onAddQuestionnaireButtonClicked)
            swipeRefreshLayout.setOnRefreshListener(vmHome::onSwipeRefreshTriggered)
            //statisticsCard.onClick(vmHome::onStatisticsCardClicked)
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

        vmHome.allQuestionnairesFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                val percentage = it.count(CompleteQuestionnaire::areAllQuestionsCorrectlyAnswered).let { correctCount ->
                    ((correctCount / it.size.toFloat())*100f).toInt()
                }
                tvCardTitle.text = getString(R.string.questionnairesPresent, it.size.toString())
                tvProgressText.text = percentage.toString().plus(getString(R.string.percentageCompleted))
                first.setBackgroundTintWithRes(if(percentage >= 25) R.color.white else R.color.unselectedColor)
                second.setBackgroundTintWithRes(if(percentage >= 50) R.color.white else R.color.unselectedColor)
                third.setBackgroundTintWithRes(if(percentage >= 75) R.color.white else R.color.unselectedColor)
                fourth.setBackgroundTintWithRes(if(percentage >= 100) R.color.white else R.color.unselectedColor)
            }
        }

        vmHome.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {

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
                ClearSearchQueryEvent -> {

                }
            }
        }
    }
}