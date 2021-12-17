package com.example.quizapp.view.fragments.adminscreens.managefaculties

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminManageFacultiesBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.fragments.resultdispatcher.setFragmentResultEventListener
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaFaculty
import com.example.quizapp.viewmodel.VmAdminManageFaculties
import com.example.quizapp.viewmodel.VmAdminManageFaculties.ManageFacultiesEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminManageFaculties: BindingFragment<FragmentAdminManageFacultiesBinding>() {

    private val vmAdmin: VmAdminManageFaculties by hiltNavDestinationViewModels(R.id.fragmentAdminManageFaculties)

    private lateinit var rvAdapter: RvaFaculty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.etSearchQuery.setText(vmAdmin.searchQuery)

        rvAdapter = RvaFaculty().apply {
            onItemLongClicked = vmAdmin::onFacultyItemClicked
            onItemClicked = vmAdmin::onFacultyItemClicked
        }

        binding.rv.apply {
            adapter = rvAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            disableChangeAnimation()
        }
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(vmAdmin::onBackButtonClicked)
            fabAdd.onClick(vmAdmin::onAddFacultyButtonClicked)
            etSearchQuery.onTextChanged(vmAdmin::onSearchQueryChanged)
            btnSearch.onClick(vmAdmin::onDeleteSearchClicked)
        }
    }

    private fun initObservers(){

        setFragmentResultEventListener(vmAdmin::onFacultyMoreOptionsSelectionResultReceived)

        setFragmentResultEventListener(vmAdmin::onDeleteFacultyConfirmationResultReceived)

        vmAdmin.facultiesStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }

        vmAdmin.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isEmpty()
            }
        }

        vmAdmin.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }
    }
}