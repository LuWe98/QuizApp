package com.example.quizapp.view.fragments.adminscreens.manageusers

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.BsdfUserMoreOptionsBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.model.menus.MenuItemDataModel
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaIntIdMenu
import com.example.quizapp.viewmodel.VmUserMoreOptions
import com.example.quizapp.viewmodel.VmUserMoreOptions.UserMoreOptionsEvent.DeleteUserEvent
import com.example.quizapp.viewmodel.VmUserMoreOptions.UserMoreOptionsEvent.NavigateToChangeUserRoleDialogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfUserMoreOptions : BindingBottomSheetDialogFragment<BsdfUserMoreOptionsBinding>() {

    private val vmOptions: VmUserMoreOptions by viewModels()

    private val args: BsdfUserMoreOptionsArgs by navArgs()

    private lateinit var rvAdapter: RvaIntIdMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        binding.tvUserName.text = args.user.userName

        rvAdapter = RvaIntIdMenu().apply {
            onItemClicked = vmOptions::onMenuItemSelected
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }

        rvAdapter.submitList(MenuItemDataModel.userMoreOptionsMenu)
    }

    private fun initObservers(){
        vmOptions.userMoreOptionsEventChannelFlow.collectWhenStarted(viewLifecycleOwner){ event ->
            when(event){
                is NavigateToChangeUserRoleDialogEvent -> navigator.navigateToChangeUserRoleDialog(event.user)
                is DeleteUserEvent -> navigator.navigateToAdminUserDeletionConfirmation(event.user)
            }
        }
    }
}