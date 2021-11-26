package com.example.quizapp.view.fragments.adminscreens.manageusers

import android.os.Bundle
import android.view.View
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminAddEditUserBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.example.quizapp.viewmodel.VmAdminAddEditUser
import com.example.quizapp.viewmodel.VmAdminAddEditUser.AddEditUserEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminAddEditUser: BindingFragment<FragmentAdminAddEditUserBinding>() {

    private val vmAdmin: VmAdminAddEditUser by hiltNavDestinationViewModels(R.id.fragmentAdminAddEditUser)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            pageTitle.setText(vmAdmin.pageTitleRes)
        }
    }

    private fun initListeners(){
        binding.apply {
            nameCard.onClick(vmAdmin::onUserNameCardClicked)
            passwordCard.onClick(vmAdmin::onUserPasswordCardClicked)
            roleCard.onClick(vmAdmin::onUserRoleCardClicked)
            btnSave.onClick(vmAdmin::onSaveButtonClicked)
            btnBack.onClick(navigator::popBackStack)
        }
    }

    private fun initObservers() {
        setUpdateStringTypeListener(UpdateStringType.USER_NAME, vmAdmin::onUserNameUpdateReceived)

        setUpdateStringTypeListener(UpdateStringType.USER_PASSWORD, vmAdmin::onUserPasswordUpdateReceived)

        setSelectionTypeListener(vmAdmin::onUserRoleUpdateReceived)

        vmAdmin.userNameStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.nameCard.text = if(it.isEmpty()) "-" else it
        }

        vmAdmin.userPasswordStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.passwordCard.text = if(it.isEmpty()) "-" else it
        }

        vmAdmin.userRoleStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.roleCard.setTextWithRes(it.textRes)
        }

        vmAdmin.addEditUserEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToUpdateStringDialog -> navigator.navigateToUpdateStringDialog(event.initialValue, event.updateType)
                is ShowMessageSnackBar -> showSnackBar(event.messageRes, anchorView = binding.btnSave)
                is NavigateToRoleSelection -> navigator.navigateToSelectionDialog(SelectionType.RoleSelection(event.currentRole))
                NavigateBackEvent -> navigator.popBackStack()
            }
        }
    }
}