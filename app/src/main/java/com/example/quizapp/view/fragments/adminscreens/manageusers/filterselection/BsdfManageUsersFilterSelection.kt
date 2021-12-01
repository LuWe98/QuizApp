package com.example.quizapp.view.fragments.adminscreens.manageusers.filterselection

import android.os.Bundle
import android.view.View
import androidx.core.view.children
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfManageUsersFilterSelectionBinding
import com.example.quizapp.databinding.ChipFilterChoiceBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.extensions.setSelectionTypeListener
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmManageUsersFilterSelection
import com.example.quizapp.viewmodel.VmManageUsersFilterSelection.ManageUsersFilterSelectionEvent.*
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfManageUsersFilterSelection: BindingBottomSheetDialogFragment<BsdfManageUsersFilterSelectionBinding>() {

    companion object {
        const val SELECTED_ROLES_RESULT_KEY = "manageUsersFilterSelectionRolesResultKey"
    }

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
            tvOrderBy.onClick(vmFilter::onOrderByCardClicked)
            tvOrderAscending.onClick(vmFilter::onOrderAscendingCardClicked)
            btnApply.onClick(vmFilter::onApplyButtonClicked)
        }
    }

    private fun initObservers(){
        setSelectionTypeListener(vmFilter::onOrderByUpdateReceived)

        vmFilter.selectedRolesStateFlow.collectWhenStarted(viewLifecycleOwner) { selectedRoles ->
            binding.roleChipGroup.children.map{ it as Chip }.forEach { chip ->
                chip.isChecked = chip.tag as Role in selectedRoles
            }
        }

        vmFilter.selectedOrderAscendingStateFlow.collectWhenStarted(viewLifecycleOwner) { ascending ->
            binding.apply {
                tvOrderAscending.setText(if(ascending) R.string.ascending else R.string.descending)
                ivOrderAscending.animate()
                    .rotation(if(ascending) 0f else 180f)
                    .setDuration(300)
                    .start()
            }
        }

        vmFilter.selectedOrderByStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                tvOrderBy.setText(it.textRes)
                ivOrderBy.setImageDrawable(it.iconRes)
            }
        }

        vmFilter.manageUsersFilterSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToSelectionScreen -> navigator.navigateToSelectionDialog(event.selectionType)
                is ApplySelectionEvent -> {
                    setFragmentResult(SELECTED_ROLES_RESULT_KEY, Bundle().apply {
                        putParcelableArray(SELECTED_ROLES_RESULT_KEY, event.selectedRoles)
                    })
                    navigator.popBackStack()
                }
            }
        }
    }
}