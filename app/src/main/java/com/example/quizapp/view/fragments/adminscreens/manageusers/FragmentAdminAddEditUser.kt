package com.example.quizapp.view.fragments.adminscreens.manageusers

import android.os.Bundle
import android.view.View
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminAddEditUserBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
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
        binding.apply {
            pageTitle.setText(vmAdmin.pageTitleRes)
            etUserName.setText(vmAdmin.userName)
            etPassword.setText(vmAdmin.userPassword)
        }
    }

    private fun initListeners(){
        binding.apply {
            roleCard.onClick(vmAdmin::onUserRoleCardClicked)
            btnSave.onClick(vmAdmin::onSaveButtonClicked)
            tvSave.onClick(vmAdmin::onSaveButtonClicked)
            btnBack.onClick(vmAdmin::onBackButtonClicked)

            etUserName.onTextChanged(vmAdmin::onUserNameChanged)
            etPassword.onTextChanged(vmAdmin::onPasswordChanged)
        }
    }

    private fun initObservers() {

        setFragmentResultEventListener(vmAdmin::onUserRoleSelectionResultReceived)

        vmAdmin.userRoleStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.roleText.setText(it.textRes)
        }

        vmAdmin.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is ShowMessageSnackBar -> showSnackBar(
                    event.messageRes,
                    anchorView = if(event.attachToActivity) null else null
                )
            }
        }
    }
}