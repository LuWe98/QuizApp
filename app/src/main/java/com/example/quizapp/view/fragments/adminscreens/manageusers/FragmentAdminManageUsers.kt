package com.example.quizapp.view.fragments.adminscreens.manageusers

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminManageUsersBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAdminUser
import com.example.quizapp.viewmodel.VmAdminManageUsers
import com.example.quizapp.viewmodel.VmAdminManageUsers.FragmentAdminEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminManageUsers : BindingFragment<FragmentAdminManageUsersBinding>() {

    private val vmAdmin: VmAdminManageUsers by hiltNavDestinationViewModels(R.id.fragmentAdminManageUsers)

    lateinit var rvAdapter: RvaAdminUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initListeners()
        initObservers()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaAdminUser().apply {
            onItemClicked = { }
            onItemLongClicked = navigator::navigateToUserMoreOptionsDialog
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
            btnBack.onClick(navigator::popBackStack)
            etSearchQuery.onTextChanged(vmAdmin::onSearchQueryChanged)
        }
    }

    private fun initObservers() {
        vmAdmin.filteredPagedData.observe(viewLifecycleOwner) {
            rvAdapter.submitData(lifecycle, it)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        vmAdmin.fragmentAdminEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is UpdateUserRoleEvent -> rvAdapter.updateUserRole(event.userId, event.newRole)
                is HideUserEvent -> rvAdapter.hideUser(event.userId)
                is ShowUserEvent -> rvAdapter.showUser(event.user)
            }
        }
    }
}