package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeCreatedBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.flowext.awareCollect
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaCreatedQuestionnaires
import com.example.quizapp.viewmodel.VmHome
import com.example.quizapp.viewmodel.VmHome.FragmentHomeCreatedEvent.ChangeCreatedSwipeRefreshLayoutVisibility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHomeCreated : BindingFragment<FragmentHomeCreatedBinding>() {

    private val vmHome: VmHome by hiltNavDestinationViewModels(R.id.fragmentHome)

    private lateinit var rvAdapter: RvaCreatedQuestionnaires

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initListeners()
        initObservers()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaCreatedQuestionnaires().apply {
            onItemClick = navigator::navigateToQuizScreen
            onItemLongClick = navigator::navigateToQuestionnaireMoreOptions
            onSyncClick = vmHome::onCreatedItemSyncButtonClicked
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }
    }

    private fun initListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener(vmHome::onSwipeRefreshCreatedQuestionnairesList)
    }

    private fun initObservers() {
        vmHome.allCreatedQuestionnairesFlow.awareCollect(viewLifecycleOwner) {
            rvAdapter.submitList(it) {
                if (it.isEmpty()) {

                }
            }
        }

        vmHome.fragmentHomeCreatedEventChannelFlow.awareCollect(viewLifecycleOwner) { event ->
            when (event) {
                is ChangeCreatedSwipeRefreshLayoutVisibility -> {
                    binding.swipeRefreshLayout.isRefreshing = event.visible
                }
            }
        }
    }
}