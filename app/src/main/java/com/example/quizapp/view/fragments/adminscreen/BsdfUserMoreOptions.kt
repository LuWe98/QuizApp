package com.example.quizapp.view.fragments.adminscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfUserMoreOptionsBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.extensions.flowext.awareCollect
import com.example.quizapp.model.menudatamodels.MenuItemDataModel
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaBsdfMenu
import com.example.quizapp.viewmodel.VmAdmin
import com.example.quizapp.viewmodel.VmUserMoreOptions
import com.example.quizapp.viewmodel.VmUserMoreOptions.UserMoreOptionsEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfUserMoreOptions : BindingBottomSheetDialogFragment<BsdfUserMoreOptionsBinding>() {

    private val vmOptions: VmUserMoreOptions by viewModels()

    private val vmAdmin : VmAdmin by hiltNavDestinationViewModels(R.id.fragmentAdmin)

    private val args: BsdfUserMoreOptionsArgs by navArgs()

    private lateinit var rvAdapter: RvaBsdfMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        binding.tvUserName.text = args.user.userName

        rvAdapter = RvaBsdfMenu().apply {
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
        vmOptions.userMoreOptionsEventChannelFlow.awareCollect(viewLifecycleOwner){ event ->
            when(event){
                is NavigateToChangeUserRoleDialogEvent -> navigator.navigateToChangeUserRoleDialog(event.user)
                is DeleteUserEvent -> vmAdmin.onDeleteUserClicked(event.user)
                NavigateBackEvent -> navigator.popBackStack()
            }
        }
    }
}