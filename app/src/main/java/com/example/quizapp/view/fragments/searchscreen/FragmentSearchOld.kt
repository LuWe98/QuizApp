package com.example.quizapp.view.fragments.searchscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.FragmentSearchBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaBrowsableQuestionnaires
import com.example.quizapp.viewmodel.VmSearchOld
import com.example.quizapp.viewmodel.VmSearchOld.FragmentSearchEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSearchOld : BindingFragment<FragmentSearchBinding>() {

    private val vmSearchOld : VmSearchOld by viewModels()

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
//        rvAdapter = RvaBrowsableQuestionnaires(vmSearchOld).apply {
//            onDownloadClick = vmSearchOld::onItemDownLoadButtonClicked
//        }

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
            btnBack.onClick(vmSearchOld::onBackButtonClicked)
            btnFilter.onClick(vmSearchOld::onFilterButtonClicked)
            etSearchQuery.onTextChanged(vmSearchOld::onSearchQueryChanged)
            swipeRefreshLayout.setOnRefreshListener(rvAdapter::refresh)
        }
    }

    private fun initObservers(){
//        vmSearchOld.filteredPagedData.collectWhenStarted(viewLifecycleOwner) {
//            binding.swipeRefreshLayout.isRefreshing = false
//            rvAdapter.submitData(lifecycle, it)
//        }

        vmSearchOld.fragmentSearchEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event){
                NavigateBack -> {
                    navigator.popBackStack()
                }
                NavigateToFilterScreen -> {

                }
                is ShowMessageSnackBar -> showSnackBar(textRes = event.messageRes)
                is ChangeItemDownloadStatusEvent -> {
                    rvAdapter.changeItemDownloadStatus(event.questionnaireId, event.downloadStatus)
                }
            }
        }
    }
}