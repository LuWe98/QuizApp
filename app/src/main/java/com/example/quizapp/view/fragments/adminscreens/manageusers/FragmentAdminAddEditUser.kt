package com.example.quizapp.view.fragments.adminscreens.manageusers

import android.os.Bundle
import android.view.View
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminAddEditUserBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.fragments.resultdispatcher.setFragmentResultEventListener
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmAdminAddEditUser
import com.example.quizapp.viewmodel.VmAdminAddEditUser.AddEditUserEvent.ShowMessageSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminAddEditUser: BindingFragment<FragmentAdminAddEditUserBinding>() {

    private val vmAdmin: VmAdminAddEditUser by hiltNavDestinationViewModels(R.id.fragmentAdminAddEditUser)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.pageTitle.setText(vmAdmin.pageTitleRes)
    }

    private fun initListeners(){
        binding.apply {
            nameCard.onClick(vmAdmin::onUserNameCardClicked)
            passwordCard.onClick(vmAdmin::onUserPasswordCardClicked)
            roleCard.onClick(vmAdmin::onUserRoleCardClicked)
            btnSave.onClick(vmAdmin::onSaveButtonClicked)
            btnBack.onClick(vmAdmin::onBackButtonClicked)
        }
    }

    private fun initObservers() {

        setFragmentResultEventListener(vmAdmin::onUserNameUpdateResultReceived)

        setFragmentResultEventListener(vmAdmin::onUserPasswordUpdateResultReceived)

        setFragmentResultEventListener(vmAdmin::onUserRoleSelectionResultReceived)

        vmAdmin.userNameStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.nameCard.text = if(it.isBlank()) "-" else it
        }

        vmAdmin.userPasswordStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.passwordCard.text = if(it.isBlank()) "-" else it
        }

        vmAdmin.userRoleStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.roleCard.setTextWithRes(it.textRes)
        }

        vmAdmin.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is ShowMessageSnackBar -> showSnackBar(
                    event.messageRes,
                    anchorView = if(event.attachToActivity) null else binding.btnSave
                )
            }
        }
    }
}