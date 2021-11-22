package com.example.quizapp.view.fragments.adminscreens.managecourseofstudies

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.BsdfAdminManageCoursesOfStudiesMoreOptionsBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.model.menus.MenuItemDataModel
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaIntIdMenu
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudiesMoreOptions
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudiesMoreOptions.FragmentAdminManageCoursesOfStudiesMoreOptionsEvent.NavigateToAddEditCourseOfStudiesScreenEvent
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudiesMoreOptions.FragmentAdminManageCoursesOfStudiesMoreOptionsEvent.ShowDeletionConfirmationDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfManageCourseOfStudiesMoreOptions: BindingBottomSheetDialogFragment<BsdfAdminManageCoursesOfStudiesMoreOptionsBinding>() {

    private val vmMoreOptions: VmAdminManageCoursesOfStudiesMoreOptions by viewModels()

    private lateinit var rvAdapter: RvaIntIdMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObservers()
    }

    private fun initViews(){
        binding.tvTitle.text = vmMoreOptions.courseOfStudiesName

        rvAdapter = RvaIntIdMenu().apply {
            onItemClicked = vmMoreOptions::onMenuItemClicked
        }

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            setHasFixedSize(true)
            disableChangeAnimation()
        }

        rvAdapter.submitList(MenuItemDataModel.courseOfStudiesMoreOptionsMenu)
    }

    private fun initObservers(){
        vmMoreOptions.fragmentAdminManageCoursesOfStudiesMoreOptionsEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToAddEditCourseOfStudiesScreenEvent -> navigator.navigateToAdminAddEditCourseOfStudies(event.courseOfStudiesWithFaculties)
                is ShowDeletionConfirmationDialog -> navigator.navigateToAdminCourseOfStudiesDeletionConfirmation(event.courseOfStudies)
            }
        }
    }
}