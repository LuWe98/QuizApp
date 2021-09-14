package com.example.quizapp.ui.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.quizapp.R
import com.example.quizapp.databinding.BasicRecyclerviewBinding
import com.example.quizapp.recyclerview.adapters.RvaQuestionnaireWithQuestions
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmHome
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHomeCachedQuestionnaires : BindingFragment<BasicRecyclerviewBinding>() {

    private val vmHome: VmHome by hiltNavGraphViewModels(R.id.main_nav_graph)

    lateinit var rvAdapter: RvaQuestionnaireWithQuestions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()

        vmHome.allQuestionnairesWithQuestionsPagingSource.observe(viewLifecycleOwner) {
            rvAdapter.submitData(lifecycle, it)
        }
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
}