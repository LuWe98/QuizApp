package com.example.quizapp.view.fragments.searchscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.FragmentSearchBinding
import com.example.quizapp.extensions.collect
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.onTextChanged
import com.example.quizapp.extensions.showKeyboard
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaBrowseQuestionnaires
import com.example.quizapp.viewmodel.VmSearch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel

@AndroidEntryPoint
class FragmentSearch : BindingFragment<FragmentSearchBinding>() {

    private val vmSearch : VmSearch by viewModels()

    private lateinit var rvAdapter : RvaBrowseQuestionnaires

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        rvAdapter = RvaBrowseQuestionnaires().apply {

        }

        binding.apply {
            etSearchQuery.requestFocus()
            showKeyboard(etSearchQuery)

            rv.apply {
                setHasFixedSize(true)
                adapter = rvAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(vmSearch::onBackButtonClicked)
            btnFilter.onClick(vmSearch::onFilterButtonClicked)
            etSearchQuery.onTextChanged(vmSearch::onSearchQueryChanged)
        }
    }

    private fun initObservers(){
        vmSearch.filteredPagedData.observe(viewLifecycleOwner) {
            rvAdapter.submitData(lifecycle, it)
        }

        vmSearch.fragmentSearchEventChannelFlow.collect(lifecycleScope) { event ->
            when(event){
                VmSearch.FragmentSearchEvent.NavigateBack -> {
                    navigator.popBackStack()
                }
                VmSearch.FragmentSearchEvent.NavigateToFilterScreen -> {
                    //TODO -> Navigate Back Stuff
                }
            }
        }
    }
}