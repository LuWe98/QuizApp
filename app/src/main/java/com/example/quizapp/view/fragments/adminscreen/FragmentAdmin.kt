package com.example.quizapp.view.fragments.adminscreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAdminPageUsers
import com.example.quizapp.viewmodel.VmAdmin
import com.example.quizapp.viewmodel.VmAdmin.FragmentAdminEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdmin: BindingFragment<FragmentAdminBinding>() {

    private val vmAdmin : VmAdmin by hiltNavDestinationViewModels(R.id.fragmentAdmin)

    lateinit var rvAdapter : RvaAdminPageUsers

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaAdminPageUsers().apply {
            onItemClicked = {

            }

            onItemLongClicked = navigator::navigateToUserMoreOptions
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }

        binding.swipeRefreshLayout.setOnRefreshListener(rvAdapter::refresh)
    }

    private fun initObservers(){
        vmAdmin.filteredPagedData.observe(viewLifecycleOwner) {
            rvAdapter.submitData(lifecycle, it)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        vmAdmin.fragmentAdminEventChannelFlow.observe(viewLifecycleOwner){ event ->
            when(event) {
                is UpdateUserRoleEvent -> rvAdapter.updateUserRole(event.userId, event.newRole)
                is HideUserEvent -> rvAdapter.hideUser(event.userId)
                is ShowUserEvent -> rvAdapter.showUser(event.user)
                is ShowUndoDeleteUserSnackBarEvent -> {
                    showSnackBar(
                        textRes = R.string.userDeleted,
                        anchorView = bindingActivity.findViewById(R.id.bottomAppBar),
                        onDismissedAction = { vmAdmin.onDeleteUserConfirmed(event) },
                        actionTextRes = R.string.undo,
                        actionClickEvent =  { vmAdmin.onUndoDeleteUserClicked(event) }
                    )
                }
            }
        }
    }
}