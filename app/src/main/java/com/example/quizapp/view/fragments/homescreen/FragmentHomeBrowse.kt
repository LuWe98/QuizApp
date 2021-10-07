package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeBrowseBinding
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaBrowseQuestionnaires
import com.example.quizapp.viewmodel.VmHome
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHomeBrowse : BindingFragment<FragmentHomeBrowseBinding>() {

    private val vmHome: VmHome by hiltNavDestinationViewModels(R.id.fragmentHome)

    private lateinit var rvAdapter : RvaBrowseQuestionnaires

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaBrowseQuestionnaires().apply {
            onDownloadClick = vmHome::onCachedItemDownLoadButtonClicked
        }

        binding.rv.apply {
            adapter = rvAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())

        }

        vmHome.allQuestionnairesFromDatabase.observe(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }
    }
}