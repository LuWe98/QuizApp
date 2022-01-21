package com.example.quizapp.view.fragments.dialogs.authorselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfAuthorSelectionLocalBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.ListLoadItemType
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAuthorSelectionLocal
import com.example.quizapp.viewmodel.VmLocalAuthorSelection
import com.example.quizapp.viewmodel.VmLocalAuthorSelection.LocalAuthorSelectionEvent.ClearSearchQueryEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfLocalAuthorSelection: BindingBottomSheetDialogFragment<BsdfAuthorSelectionLocalBinding>() {

    private val vmAuthor: VmLocalAuthorSelection by viewModels()

    private lateinit var rvAdapter: RvaAuthorSelectionLocal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initObservers()
        initListeners()
    }

    private fun initViews(){
        binding.etSearchQuery.setText(vmAuthor.searchQuery)

        rvAdapter = RvaAuthorSelectionLocal().apply {
            onItemClicked = vmAuthor::onAuthorClicked
            selectionPredicate = vmAuthor::isAuthorSelected
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
            etSearchQuery.onTextChanged(vmAuthor::onSearchQueryChanged)
            btnConfirm.onClick(vmAuthor::onConfirmButtonClicked)
            btnSearch.onClick(vmAuthor::onDeleteSearchQueryClicked)
            btnCollapse.onClick(vmAuthor::onCollapseButtonClicked)
        }
    }

    private fun initObservers(){
        vmAuthor.selectedAuthorIdsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.rv.updateAllViewHolders()
        }

        vmAuthor.filteredAuthorInfos.collectWhenStarted(viewLifecycleOwner) {
            it.adjustVisibilities(
                binding.rv,
                binding.dataAvailability,
                ListLoadItemType.LOCAL_AUTHOR
            )
            rvAdapter.submitList(it.data)
        }

        vmAuthor.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isEmpty()
            }
        }

        vmAuthor.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}