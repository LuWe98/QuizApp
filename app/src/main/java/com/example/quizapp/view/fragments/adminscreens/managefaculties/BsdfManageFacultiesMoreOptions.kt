package com.example.quizapp.view.fragments.adminscreens.managefaculties

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.BsdfAdminManageFacultiesMoreOptionsBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.model.menus.MenuItemDataModel
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaIntIdMenu
import com.example.quizapp.viewmodel.VmAdminManageFacultiesMoreOptions
import com.example.quizapp.viewmodel.VmAdminManageFacultiesMoreOptions.FragmentAdminManageFacultiesMoreOptionsEvent.NavigateToAddEditFacultyScreenEvent
import com.example.quizapp.viewmodel.VmAdminManageFacultiesMoreOptions.FragmentAdminManageFacultiesMoreOptionsEvent.ShowDeletionConfirmationDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfManageFacultiesMoreOptions: BindingBottomSheetDialogFragment<BsdfAdminManageFacultiesMoreOptionsBinding>() {

    private val vmMoreOptions: VmAdminManageFacultiesMoreOptions by viewModels()

    private lateinit var rvAdapter: RvaIntIdMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObservers()
    }

    private fun initViews(){
        binding.tvTitle.text = vmMoreOptions.facultyName

        rvAdapter = RvaIntIdMenu().apply {
            onItemClicked = vmMoreOptions::onMenuItemClicked
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }

        rvAdapter.submitList(MenuItemDataModel.facultyMoreOptionsMenu)
    }

    private fun initObservers() {
        vmMoreOptions.fragmentAdminManageFacultiesMoreOptionsEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToAddEditFacultyScreenEvent -> navigator.navigateToAdminAddEditFaculty(event.faculty)
                is ShowDeletionConfirmationDialog -> navigator.navigateToAdminFacultyDeletionConfirmation(event.faculty)
            }
        }
    }
}