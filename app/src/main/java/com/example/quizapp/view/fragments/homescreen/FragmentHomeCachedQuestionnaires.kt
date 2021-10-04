package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeCachedBinding
import com.example.quizapp.extensions.collect
import com.example.quizapp.view.recyclerview.adapters.RvaQuestionnaireWithQuestions
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmHome
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class FragmentHomeCachedQuestionnaires : BindingFragment<FragmentHomeCachedBinding>() {

    private val vmHome: VmHome by hiltNavGraphViewModels(R.id.main_nav_graph)

    lateinit var rvAdapter: RvaQuestionnaireWithQuestions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        iniObservers()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaQuestionnaireWithQuestions().apply {
            onItemClick = {
                navigator.navigateToQuizScreen(it.id)
            }
            onItemLongClick = {
                navigator.navigateToAddQuestionnaireScreen(it.id)
            }
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun iniObservers() {
        vmHome.allQuestionnairesWithQuestionsPagingData.observe(viewLifecycleOwner) {
            rvAdapter.submitData(lifecycle, it)
        }

        rvAdapter.loadStateFlow.map { it.refresh }.distinctUntilChanged().collect(lifecycleScope) {
            if (it is LoadState.NotLoading) {
                //binding.noDataLayout.isVisible = rvAdapter.itemCount == 0
            }
        }
    }
}