package com.example.quizapp.view.fragments.adminscreens.managecourseofstudies

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.example.quizapp.R
import com.example.quizapp.databinding.DialogCustomAlertBinding
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.extensions.onClick
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfAdminCourseOfStudiesDeletionConfirmation: BindingDialogFragment<DialogCustomAlertBinding>() {

    private val vmAdmin: VmAdminManageCoursesOfStudies by hiltNavDestinationViewModels(R.id.fragmentAdminManageCourseOfStudies)

    private val args: DfAdminCourseOfStudiesDeletionConfirmationArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews(){
        binding.apply {
            tvTitle.setText(R.string.deletionConfirmationTile)
            tvText.setText(R.string.warningCourseOfStudiesDeletetion)
        }
    }

    private fun initListeners(){
        binding.apply {
            btnCancel.onClick(navigator::popBackStack)

            btnConfirm.onClick {
                vmAdmin.onDeleteCourseOfStudiesConfirmed(args.courseOfStudies)
                navigator.popBackStack()
            }
        }
    }
}