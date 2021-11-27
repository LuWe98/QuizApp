package com.example.quizapp.view.fragments.adminscreens.managefaculties

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminManageFacultiesBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.DfCustomLoading
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.view.recyclerview.adapters.RvaFaculty
import com.example.quizapp.viewmodel.VmAdminManageFaculties
import com.example.quizapp.viewmodel.VmAdminManageFaculties.FragmentAdminManageFacultiesEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminManageFaculties: BindingFragment<FragmentAdminManageFacultiesBinding>() {

    private val vmAdmin: VmAdminManageFaculties by hiltNavDestinationViewModels(R.id.fragmentAdminManageFaculties)

    private lateinit var rvAdapter: RvaFaculty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            btnBack.onClick(navigator::popBackStack)
            fabAdd.onClick(navigator::navigateToAdminAddEditFaculty)
            etSearchQuery.onTextChanged(vmAdmin::onSearchQueryChanged)
            btnSearch.onClick(vmAdmin::onDeleteSearchClicked)
        }
    }

    private fun initObservers(){
        setSelectionTypeWithParsedValueListener(vmAdmin::onMoreOptionsItemSelected)

        setConfirmationTypeListener(vmAdmin::onDeleteFacultyConfirmed)

        vmAdmin.facultiesStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }

        vmAdmin.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isEmpty()
            }
        }

        vmAdmin.fragmentAdminManageFacultiesEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                NavigateBack -> navigator.popBackStack()
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
                is NavigateToFacultiesMoreOptionsDialogEvent -> navigator.navigateToSelectionDialog(SelectionType.FacultyMoreOptionsSelection(event.faculty))
                is NavigateToAddEditFacultyEvent -> navigator.navigateToAdminAddEditFaculty(event.faculty)
                is NavigateToDeletionConfirmationEvent -> navigator.navigateToConfirmationDialog(ConfirmationType.DeleteFacultyConfirmation(event.faculty))
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
                is ChangeProgressVisibilityEvent -> {
                    if(event.visible) {
                        showDialog<DfCustomLoading>("test").apply {
                            isCancelable = false
                        }
                    } else {
                        findDialog<DfCustomLoading>("test")?.dismiss()
                    }
                }
            }
        }
    }
}