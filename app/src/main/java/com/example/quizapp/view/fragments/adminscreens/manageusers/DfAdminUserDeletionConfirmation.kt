package com.example.quizapp.view.fragments.adminscreens.manageusers

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.example.quizapp.R
import com.example.quizapp.databinding.DialogCustomAlertBinding
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.extensions.onClick
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmAdminManageUsers
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfAdminUserDeletionConfirmation: BindingDialogFragment<DialogCustomAlertBinding>() {

    private val vmAdmin: VmAdminManageUsers by hiltNavDestinationViewModels(R.id.fragmentAdminManageUsers)

    private val args: DfAdminUserDeletionConfirmationArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews(){
        binding.apply {
            tvTitle.setText(R.string.deletionConfirmationTile)
            tvText.setText(R.string.warningUserDeletetion)
        }
    }

    private fun initListeners(){
        binding.apply {
            btnCancel.onClick(navigator::popBackStack)

            btnConfirm.onClick {
                navigator.popBackStack()
                vmAdmin.onDeleteUserConfirmed(args.user)
            }
        }
    }
}