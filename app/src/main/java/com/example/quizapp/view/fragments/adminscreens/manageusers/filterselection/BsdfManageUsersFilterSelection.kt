package com.example.quizapp.view.fragments.adminscreens.manageusers.filterselection

import android.os.Bundle
import android.view.View
import androidx.core.view.children
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfManageUsersFilterSelectionBinding
import com.example.quizapp.databinding.ChipFilterChoiceBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.viewmodel.VmManageUsersFilterSelection
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfManageUsersFilterSelection: BindingBottomSheetDialogFragment<BsdfManageUsersFilterSelectionBinding>() {

    private val vmFilter: VmManageUsersFilterSelection by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChips()
        initListeners()
        initObservers()
    }

    private fun initChips(){
        binding.roleChipGroup.let { chipGroup ->
            Role.values().forEach { role ->
                ChipFilterChoiceBinding.inflate(layoutInflater).root.apply {
                    isChecked = vmFilter.isRoleSelected(role)
                    tag = role
                    text = getString(role.textRes)
                    setOnClickListener {
                        vmFilter.onRoleChipClicked(it.tag as Role)
                    }
                    chipGroup.addView(this)
                }
            }
        }
    }

    private fun initListeners(){
        binding.apply {
            orderByCard.onClick(vmFilter::onOrderByCardClicked)
            ascendingLayout.onClick(vmFilter::onOrderAscendingCardClicked)
            btnApply.onClick(vmFilter::onApplyButtonClicked)
        }
    }

    private fun initObservers(){

        setFragmentResultEventListener(vmFilter::onOrderBySelectionResultReceived)

        vmFilter.selectedRolesStateFlow.collectWhenStarted(viewLifecycleOwner) { selectedRoles ->
            binding.roleChipGroup.children.map{ it as Chip }.forEach { chip ->
                chip.isChecked = chip.tag as Role in selectedRoles
            }
        }

        vmFilter.selectedOrderAscendingStateFlow.collectWhenStarted(viewLifecycleOwner) { ascending ->
            binding.ascendingSwitch.isChecked = ascending
        }

        vmFilter.selectedOrderByStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.orderByText.setText(it.textRes)
        }
    }
}