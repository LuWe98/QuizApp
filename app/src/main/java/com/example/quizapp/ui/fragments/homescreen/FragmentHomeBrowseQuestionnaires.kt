package com.example.quizapp.ui.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.quizapp.R
import com.example.quizapp.databinding.BasicRecyclerviewBinding
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmHome
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHomeBrowseQuestionnaires : BindingFragment<BasicRecyclerviewBinding>() {

    private val vmHome: VmHome by hiltNavGraphViewModels(R.id.main_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }
}