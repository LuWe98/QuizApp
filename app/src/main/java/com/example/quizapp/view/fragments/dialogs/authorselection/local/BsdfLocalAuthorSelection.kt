package com.example.quizapp.view.fragments.dialogs.authorselection.local

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfAuthorSelectionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAuthorSelectionLocal
import com.example.quizapp.viewmodel.VmLocalAuthorSelection
import com.example.quizapp.viewmodel.VmLocalAuthorSelection.LocalAuthorSelectionEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.VmLocalAuthorSelection.LocalAuthorSelectionEvent.SendResultEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfLocalAuthorSelection: BindingBottomSheetDialogFragment<BsdfAuthorSelectionBinding>() {

    companion object {
        const val AUTHOR_SELECTION_RESULT_KEY = "localAuthorSelectionResultKey"
    }

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
        }
    }

    private fun initObservers(){
        vmAuthor.selectedAuthorIdsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.rv.updateAllViewHolders()
        }

        vmAuthor.filteredAuthorInfos.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }

        vmAuthor.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isBlank()
            }
        }

        vmAuthor.userCreatorSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is SendResultEvent -> {
                    setFragmentResult(AUTHOR_SELECTION_RESULT_KEY, Bundle().apply {
                        putStringArray(AUTHOR_SELECTION_RESULT_KEY, event.selectedAuthorIds)
                    })
                    navigator.popBackStack()
                }
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}