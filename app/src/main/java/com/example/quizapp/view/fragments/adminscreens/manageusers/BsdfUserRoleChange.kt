package com.example.quizapp.view.fragments.adminscreens.manageusers

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfUserRoleSelectionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.model.ktor.status.Resource
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.viewmodel.VmAdminManageUsers
import com.example.quizapp.viewmodel.VmChangeUserRole
import com.google.android.material.radiobutton.MaterialRadioButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfUserRoleChange : BindingBottomSheetDialogFragment<BsdfUserRoleSelectionBinding>() {

    private val vmRole: VmChangeUserRole by viewModels()

    private val vmAdminManageUsers : VmAdminManageUsers by hiltNavDestinationViewModels(R.id.fragmentAdminManageUsers)

    private val args: BsdfUserRoleChangeArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            tvTitle.text = getString(R.string.changeRoleForUser, args.user.userName)

            roleRadioGroup.apply {
                Role.values().forEach { role ->
                    MaterialRadioButton(requireContext()).apply {
                        generateDefaultLayoutParams(this)
                        buttonTintList = ColorStateList.valueOf(getThemeColor(R.attr.colorControlNormal))
                        text = role.name
                    }.let { child ->
                        addView(child)
                        if(role == args.user.role){
                            check(child.id)
                        }
                    }
                }
            }
        }
    }

    private fun generateDefaultLayoutParams(view: View) = view.apply {
        layoutParams = RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 10, 0,0)
            setPadding(25, 0, 0, 0)
        }
    }

    private fun initListeners(){
        binding.apply {
            btnSave.onClick {
                Role.valueOf(roleRadioGroup.getSelectedButton().textAsString()).let { newRole ->
                    vmRole.onSaveButtonClicked(newRole)
                }
            }
        }
    }

    private fun initObservers(){
        vmRole.fragmentChangeUserRoleEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                VmChangeUserRole.FragmentChangeUserRoleEvent.NavigateBack -> {
                    navigator.popBackStack()
                }
                is VmChangeUserRole.FragmentChangeUserRoleEvent.StateTest -> {
                    when(event.resources){
                        is Resource.Loading -> {
                            dialog?.setCancelable(false)
                            binding.apply {
                                btnSave.isEnabled = false
                                roleRadioGroup.isEnabled = false
                                roleRadioGroup.visibility = View.INVISIBLE
                                progress.isVisible = true
                            }
                        }
                        is Resource.Error -> {
                            dialog?.setCancelable(true)
                            binding.apply {
                                btnSave.isEnabled = true
                                roleRadioGroup.isEnabled = true
                                roleRadioGroup.visibility = View.VISIBLE
                                progress.isVisible = false
                            }
                        }
                        is Resource.Success -> {
                            dialog?.setCancelable(true)
                            event.resources.data?.let { user ->
                                vmAdminManageUsers.onUserRoleSuccessfullyChanged(user.id, user.role)
                            }
                        }
                    }
                }
            }
        }
    }
}