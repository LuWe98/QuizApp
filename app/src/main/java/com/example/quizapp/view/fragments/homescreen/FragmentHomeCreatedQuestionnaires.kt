package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeCreatedBinding
import com.example.quizapp.extensions.launchWhenStarted
import com.example.quizapp.view.fragments.bindingfragmentsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaCreated
import com.example.quizapp.viewmodel.VmHome
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class FragmentHomeCreatedQuestionnaires : BindingFragment<FragmentHomeCreatedBinding>() {

    private val vmHome: VmHome by hiltNavGraphViewModels(R.id.main_nav_graph)

    private lateinit var rvAdapter : RvaCreated

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaCreated().apply {

        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }

        launchWhenStarted {
            vmHome.allQuestionnairesForUser().collect {
                rvAdapter.submitList(it)
            }
        }
    }
}