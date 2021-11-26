package com.example.quizapp.view.fragments.dialogs.usercreatorselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfUserCreatorSelectionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaUserCreatorBrowse
import com.example.quizapp.viewmodel.VmUserCreatorSelection
import com.example.quizapp.viewmodel.VmUserCreatorSelection.UserCreatorSelectionEvent.SendResultEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfUserCreatorSelection: BindingBottomSheetDialogFragment<BsdfUserCreatorSelectionBinding>() {

    companion object {
        const val USER_SELECTION_RESULT_KEY = "userCreatorSelectionResultKey"
    }

    private val vmUser: VmUserCreatorSelection by viewModels()

    private lateinit var rvAdapter: RvaUserCreatorBrowse

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initObservers()
        initListeners()
    }

    private fun initViews(){
        binding.etSearchQuery.setText(vmUser.searchQuery)

        rvAdapter = RvaUserCreatorBrowse().apply {
            onItemClicked = vmUser::onUserClicked
            selectionPredicate = vmUser::isUserSelected
            selectionColor = getThemeColor(R.attr.colorOnBackground)
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }
    }

    private fun initListeners(){
        binding.apply {
            etSearchQuery.onTextChanged(vmUser::onSearchQueryChanged)
            btnConfirm.onClick(vmUser::onConfirmButtonClicked)
        }
    }

    private fun initObservers(){
        vmUser.selectedUsersStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.rv.updateAllViewHolders()
        }

        vmUser.filteredPagedDataStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitData(it)
        }

        vmUser.userCreatorSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is SendResultEvent -> {
                    setFragmentResult(USER_SELECTION_RESULT_KEY, Bundle().apply {
                        putParcelableArray(USER_SELECTION_RESULT_KEY, event.selectedUsers)
                    })
                    navigator.popBackStack()
                }
            }
        }
    }
}