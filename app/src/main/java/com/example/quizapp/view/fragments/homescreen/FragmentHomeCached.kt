package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeCachedBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.extensions.observe
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaCachedQuestionnaires
import com.example.quizapp.viewmodel.VmHome
import com.example.quizapp.viewmodel.VmHome.FragmentHomeCachedEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHomeCached : BindingFragment<FragmentHomeCachedBinding>() {

    private val vmHome: VmHome by hiltNavDestinationViewModels(R.id.fragmentHome)

    private lateinit var rvAdapter: RvaCachedQuestionnaires

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initListeners()
        initObservers()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaCachedQuestionnaires().apply {
            onItemClick = navigator::navigateToQuizScreen
            onItemLongClick = navigator::navigateToQuestionnaireMoreOptions
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            disableChangeAnimation()
        }
    }

    private fun initListeners(){
        binding.swipeRefreshLayout.setOnRefreshListener(vmHome::onSwipeRefreshCachedQuestionnairesList)
    }

    private fun initObservers() {
        vmHome.allCachedQuestionnairesLD.observe(viewLifecycleOwner) {
            rvAdapter.submitList(it){
                if(it.isEmpty()){

                }
            }
        }

        vmHome.fragmentHomeCachedEventChannelFlow.observe(viewLifecycleOwner){ event ->
            when(event) {
                is ChangeCachedSwipeRefreshLayoutVisibility -> {
                    binding.swipeRefreshLayout.isRefreshing = event.visible
                }
            }
        }
    }
}