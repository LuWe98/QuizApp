package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeCachedBinding
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaQuestionnaireWithQuestions
import com.example.quizapp.viewmodel.VmHome
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHomeCachedQuestionnaires : BindingFragment<FragmentHomeCachedBinding>() {

    private val vmHome: VmHome by hiltNavDestinationViewModels(R.id.fragmentHome)

    private lateinit var rvAdapter: RvaQuestionnaireWithQuestions

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
                //navigator.navigateToAddQuestionnaireScreen(it.id)
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
        vmHome.allQuestionnairesWithQuestionsLD.observe(viewLifecycleOwner, rvAdapter::submitList)
    }
}