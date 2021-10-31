package com.example.quizapp.view.fragments.searchscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.FragmentSearchBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.flowext.collect
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaBrowsableQuestionnaires
import com.example.quizapp.viewmodel.VmSearch
import com.example.quizapp.viewmodel.VmSearch.FragmentSearchEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSearch : BindingFragment<FragmentSearchBinding>() {

    private val vmSearch : VmSearch by viewModels()

    private lateinit var rvAdapter : RvaBrowsableQuestionnaires

    //            etSearchQuery.requestFocus()
//            showKeyboard(etSearchQuery)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        rvAdapter = RvaBrowsableQuestionnaires().apply {
            onDownloadClick = vmSearch::onItemDownLoadButtonClicked
        }

        binding.apply {
            rv.apply {
                setHasFixedSize(true)
                adapter = rvAdapter
                layoutManager = LinearLayoutManager(requireContext())
                disableChangeAnimation()
            }
        }
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(vmSearch::onBackButtonClicked)
            btnFilter.onClick(vmSearch::onFilterButtonClicked)
            etSearchQuery.onTextChanged(vmSearch::onSearchQueryChanged)
            swipeRefreshLayout.setOnRefreshListener(rvAdapter::refresh)
        }
    }

    private fun initObservers(){
        vmSearch.filteredPagedData.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            rvAdapter.submitData(lifecycle, it)
        }

        vmSearch.fragmentSearchEventChannelFlow.collect(lifecycleScope) { event ->
            when(event){
                NavigateBack -> {
                    navigator.popBackStack()
                }
                NavigateToFilterScreen -> {
                    //TODO -> Navigate Back Stuff
                }
                is ShowMessageSnackBar -> {
                    showSnackBar(textRes = event.messageRes)
                }
                is ChangeItemDownloadStatusEvent -> {
                    rvAdapter.changeItemDownloadStatus(event.questionnaireId, event.downloadStatus)
                }
            }
        }
    }
}