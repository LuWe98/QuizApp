package com.example.quizapp.view.fragments.adminscreens.manageusers

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminManageUsersBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.view.fragments.resultdispatcher.setFragmentResultEventListener
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAdminUser
import com.example.quizapp.viewmodel.VmAdminManageUsers
import com.example.quizapp.viewmodel.VmAdminManageUsers.ManageUsersEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminManageUsers : BindingFragment<FragmentAdminManageUsersBinding>() {

    private val vmAdmin: VmAdminManageUsers by hiltNavDestinationViewModels(R.id.fragmentAdminManageUsers)

    lateinit var rvAdapter: RvaAdminUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initRecyclerView()
        initListeners()
        initObservers()
    }

    private fun initRecyclerView() {
        binding.etSearchQuery.setText(vmAdmin.searchQuery)

        rvAdapter = RvaAdminUser().apply {
            onItemClicked = vmAdmin::onUserItemClicked
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }

        binding.swipeRefreshLayout.setOnRefreshListener(rvAdapter::refresh)
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(vmAdmin::onBackButtonClicked)
            etSearchQuery.onTextChanged(vmAdmin::onSearchQueryChanged)
            fabAdd.onClick(vmAdmin::onAddUserButtonClicked)
            btnSearch.onClick(vmAdmin::onClearSearchQueryClicked)
            btnFilter.onClick(vmAdmin::onFilterButtonClicked)
        }
    }

    private fun initObservers() {

        setFragmentResultEventListener(vmAdmin::onUserMoreOptionsSelectionResultReceived)

        setFragmentResultEventListener(vmAdmin::onDeleteUserConfirmationResultReceived)

        setFragmentResultEventListener(vmAdmin::onMangeUsersFilterUpdateReceived)

        vmAdmin.filteredPagedDataStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitData(lifecycle, it)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        vmAdmin.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isBlank()
            }
        }

        vmAdmin.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is UpdateUserRoleEvent -> rvAdapter.updateUserRole(event.userId, event.newRole)
                is HideUserEvent -> rvAdapter.hideUser(event.userId)
                is ShowUserEvent -> rvAdapter.showUser(event.user)
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
                is ShowMessageSnackBarEvent -> showSnackBar(event.messageRes)
            }
        }
    }
}